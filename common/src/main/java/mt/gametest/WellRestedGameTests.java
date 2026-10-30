package mt.gametest;

import mt.config.MidnightThoughtsConfig;
import mt.gametest.TestContext.Bed;
import mt.gametest.TestContext.Decor;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.WellRestedPacket;
import mt.platform.MTPlatform;
import mt.server.WellRestedEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

@SuppressWarnings("resource")
final class WellRestedGameTests {

    private static final String ID = "well_rested";
    private static final String TICKS_KEY = "mt_well_rested_ticks_remaining";

    private WellRestedGameTests() {}

    static double bonus(ServerPlayer player, Holder<Attribute> attribute) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return Double.NaN;
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (modifier.id().getNamespace().equals("midnightthoughts")) return modifier.amount();
        }
        return Double.NaN;
    }

    static int speedModifiers(ServerPlayer player) {
        AttributeInstance instance = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance == null) return 0;
        int count = 0;
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (modifier.id().getNamespace().equals("midnightthoughts")) count++;
        }
        return count;
    }

    static boolean hasNoBonus(ServerPlayer player) {
        for (Holder.Reference<Attribute> attribute : BuiltInRegistries.ATTRIBUTE.holders().toList()) {
            if (!Double.isNaN(bonus(player, attribute))) return false;
        }
        return true;
    }

    static void setTicksLeft(MockPlayer player, int ticks) {
        MTPlatform.persistentData(player.get()).putInt(TICKS_KEY, ticks);
    }

    static int amplifier(ServerPlayer player, Holder<MobEffect> effect) {
        MobEffectInstance instance = player.getEffect(effect);
        return instance == null ? -1 : instance.getAmplifier();
    }

    static MidnightThoughtsConfig.WellRestedLevel levelOne(TestContext ctx) {
        return ctx.config().getWellRested().getLevel(1);
    }

    static void register(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        categories.add(new TestCatalog.Category(ID, "Well Rested and MVP", List.of(
                "comfort 1 to 5 gives levels 1 to 5, above 5 gives 5, 0 or less gives nothing",
                "the effect runs in three phases with weaker bonuses and ends cleanly",
                "shortening the duration on the fly restarts the HUD at full strength",
                "the effect survives relogging, ends on death and never leaks between profiles",
                "the effect keeps running with its bonuses while the player travels to the nether and back",
                "a new night replaces the running effect instead of stacking a second set of bonuses",
                "the MVP gets the MVP effect, every on and off mix of MVP and Well Rested behaves",
                "custom attributes and effects can be added, removed and tuned, unknown ids are skipped",
                "health is topped up when the effect starts and the regeneration bonus heals")));

        cases.add(new GameTestCase(ID, "comfort_to_level", "Comfort 1 to 5 gives levels 1 to 5, the rest is clamped", 100, ctx -> {
            MockPlayer player = ctx.join("Rested");
            ctx.then(() -> {
                int[] minutes = {3, 5, 7, 10, 15};
                for (int comfort = 1; comfort <= 5; comfort++) {
                    WellRestedEffect.applyToPlayer(player.get(), comfort);
                    ctx.equal(comfort, WellRestedEffect.getLevel(player.get()), "level for comfort " + comfort);
                    ctx.equal(minutes[comfort - 1] * 1200, WellRestedEffect.getTicksRemaining(player.get()), "duration for comfort " + comfort);
                }
                WellRestedEffect.applyToPlayer(player.get(), 8);
                ctx.equal(5, WellRestedEffect.getLevel(player.get()), "level for comfort 8");
                for (int comfort : new int[]{0, -3}) {
                    WellRestedEffect.clear(player.get());
                    WellRestedEffect.applyToPlayer(player.get(), comfort);
                    ctx.check(!WellRestedEffect.hasEffect(player.get()), "comfort " + comfort + " gave an effect");
                }
            });
        }));

        cases.add(new GameTestCase(ID, "three_phases", "Three phases weaken the bonuses and the effect ends cleanly", 200, ctx -> {
            MockPlayer player = ctx.join("Fading");
            ctx.then(() -> {
                WellRestedEffect.applyToPlayer(player.get(), 1);
                ctx.near(0.08F, bonus(player.get(), Attributes.MOVEMENT_SPEED), "speed bonus in phase 1");
                setTicksLeft(player, 2401);
            }).idle(2).then(() -> {
                ctx.equal(1, WellRestedEffect.getCurrentPhase(player.get()), "phase after a third");
                ctx.near(0.04F, bonus(player.get(), Attributes.MOVEMENT_SPEED), "speed bonus in phase 2");
                setTicksLeft(player, 1201);
            }).idle(2).then(() -> {
                ctx.equal(2, WellRestedEffect.getCurrentPhase(player.get()), "phase after two thirds");
                ctx.near(0.02F, bonus(player.get(), Attributes.MOVEMENT_SPEED), "speed bonus in phase 3");
                setTicksLeft(player, 1);
            }).idle(3).then(() -> {
                ctx.check(!WellRestedEffect.hasEffect(player.get()), "the effect did not end");
                ctx.check(hasNoBonus(player.get()), "a bonus was left behind");
                ctx.check(!ctx.lastSent(player, WellRestedPacket.class).active(), "the HUD was not told the effect ended");
            });
        }));

        cases.add(new GameTestCase(ID, "shorter_duration_on_the_fly", "Shortening the duration restarts the HUD at full strength", 200, ctx -> {
            MockPlayer player = ctx.join("Adjusted");
            ctx.then(() -> {
                WellRestedEffect.applyToPlayer(player.get(), 1);
                setTicksLeft(player, 2400);
            }).idle(2).then(() -> {
                ctx.equal(1, WellRestedEffect.getCurrentPhase(player.get()), "phase before the change");
                levelOne(ctx).durationMinutes = 1;
            }).idle(2).then(() -> {
                int left = WellRestedEffect.getTicksRemaining(player.get());
                ctx.check(left <= 1200 && left >= 1190, "time left after the change: " + left);
                ctx.equal(0, WellRestedEffect.getCurrentPhase(player.get()), "phase after the change");
                ctx.near(0.08F, bonus(player.get(), Attributes.MOVEMENT_SPEED), "speed bonus after the change");
                WellRestedPacket hud = ctx.lastSent(player, WellRestedPacket.class);
                ctx.equal(1200, hud.totalTicks(), "duration shown by the HUD");
                ctx.check(hud.ticksRemaining() >= 1190, "the HUD is not nearly full: " + hud.ticksRemaining());
            });
        }));

        cases.add(new GameTestCase(ID, "survives_relogging", "Relogging keeps the effect, its phase and its bonuses", 200, ctx -> {
            MockPlayer[] player = {ctx.join("Returning")};
            int[] before = new int[1];
            ctx.then(() -> WellRestedEffect.applyToPlayer(player[0].get(), 3))
                    .idle(10)
                    .then(() -> {
                        before[0] = WellRestedEffect.getTicksRemaining(player[0].get());
                        player[0] = ctx.rejoin(player[0], ctx.at(10, 1, 4));
                    })
                    .idle(1)
                    .then(() -> {
                        ServerPlayer back = player[0].get();
                        ctx.check(WellRestedEffect.hasEffect(back), "the effect was lost on relog");
                        ctx.equal(3, WellRestedEffect.getLevel(back), "level after relog");
                        int left = WellRestedEffect.getTicksRemaining(back);
                        ctx.check(left <= before[0] && left >= before[0] - 5, "time left jumped on relog: " + before[0] + " to " + left);
                        ctx.near(0.16F, bonus(back, Attributes.MOVEMENT_SPEED), "speed bonus after relog");
                    });
        }));

        cases.add(new GameTestCase(ID, "survives_dimension_travel", "The effect keeps running in the nether and back", 200, ctx -> {
            MockPlayer player = ctx.join("Traveller");
            int[] ticks = new int[1];
            ctx.then(() -> {
                WellRestedEffect.applyToPlayer(player.get(), 3);
                ticks[0] = WellRestedEffect.getTicksRemaining(player.get());
                player.moveTo(ctx.nether(), new BlockPos(0, 100, 0));
            }).idle(5).then(() -> {
                ctx.check(player.get().level() == ctx.nether(), "the player did not reach the nether");
                ctx.check(WellRestedEffect.hasEffect(player.get()), "the effect was lost in the nether");
                ctx.check(WellRestedEffect.getTicksRemaining(player.get()) < ticks[0], "the effect stopped counting down in the nether");
                ctx.near(0.16F, bonus(player.get(), Attributes.MOVEMENT_SPEED), "speed bonus in the nether");
                ctx.check(ctx.lastSent(player, WellRestedPacket.class).active(), "the HUD lost the effect in the nether");
                player.moveTo(ctx.level(), ctx.at(10, 1, 4));
            }).idle(2).then(() -> {
                ctx.check(player.get().level() == ctx.level(), "the player did not come back");
                ctx.equal(3, WellRestedEffect.getLevel(player.get()), "level after the trip");
                ctx.near(0.16F, bonus(player.get(), Attributes.MOVEMENT_SPEED), "speed bonus after the trip");
            });
        }));

        cases.add(new GameTestCase(ID, "new_night_replaces_effect", "A new night replaces the running effect", 1200, ctx -> {
            Bed bed = SleepGameTests.litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Regular", bed);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the first night", player)
                    .then(() -> {
                        ctx.equal(1, WellRestedEffect.getLevel(player.get()), "level after the first night");
                        setTicksLeft(player, 300);
                    })
                    .idle(3)
                    .then(() -> {
                        ctx.equal(2, WellRestedEffect.getCurrentPhase(player.get()), "phase before the second night");
                        ctx.decorate(bed, Decor.LIGHTING, Decor.CARPET, Decor.FURNITURE, Decor.DECORATION);
                        ctx.night();
                    })
                    .idle(2)
                    .then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the second night", player)
                    .then(() -> {
                        ctx.equal(4, WellRestedEffect.getLevel(player.get()), "level after the second night");
                        ctx.equal(0, WellRestedEffect.getCurrentPhase(player.get()), "phase after the second night");
                        ctx.check(WellRestedEffect.getTicksRemaining(player.get()) > 10 * 1200 - 20, "the new effect did not start at full length");
                        ctx.equal(1, speedModifiers(player.get()), "speed bonuses on the player");
                        ctx.near(0.20F, bonus(player.get(), Attributes.MOVEMENT_SPEED), "speed bonus of the new night");
                    });
        }));

        cases.add(new GameTestCase(ID, "death_clears", "Death removes the effect and its bonuses", 100, ctx -> {
            MockPlayer player = ctx.join("Unlucky");
            ctx.then(() -> {
                WellRestedEffect.applyToPlayer(player.get(), 2);
                player.get().kill();
            }).idle(2).then(() -> {
                ctx.check(!WellRestedEffect.hasEffect(player.get()), "the effect survived death");
                ctx.check(hasNoBonus(player.get()), "a bonus survived death");
                ctx.equal(1, ApiEventLog.of("expired", player.uuid()).size(), "expired events fired");
            });
        }));

        cases.add(new GameTestCase(ID, "profiles_stay_apart", "Two profiles keep their own effect and never share it", 200, ctx -> {
            MockPlayer[] first = {ctx.join("First")};
            MockPlayer[] second = new MockPlayer[1];
            ctx.then(() -> {
                WellRestedEffect.applyToPlayer(first[0].get(), 4);
                first[0].leave();
                second[0] = ctx.join("Second");
            }).idle(1).then(() -> {
                ctx.check(!WellRestedEffect.hasEffect(second[0].get()), "the second profile got the first one's effect");
                ctx.check(hasNoBonus(second[0].get()), "the second profile got the first one's bonuses");
                WellRestedEffect.applyToPlayer(second[0].get(), 2);
                second[0].leave();
                first[0] = ctx.rejoin(first[0], ctx.at(10, 1, 4));
            }).idle(1).then(() -> {
                ctx.equal(4, WellRestedEffect.getLevel(first[0].get()), "first profile level after coming back");
                ctx.near(0.20F, bonus(first[0].get(), Attributes.MOVEMENT_SPEED), "first profile speed bonus");
                first[0].leave();
                second[0] = ctx.rejoin(second[0], ctx.at(10, 1, 4));
            }).idle(1).then(() -> {
                ctx.equal(2, WellRestedEffect.getLevel(second[0].get()), "second profile level after coming back");
                ctx.near(0.12F, bonus(second[0].get(), Attributes.MOVEMENT_SPEED), "second profile speed bonus");
            });
        }));

        cases.add(new GameTestCase(ID, "mvp_effect_and_profiles", "The MVP effect lasts its own duration and stays with its owner", 200, ctx -> {
            MockPlayer[] mvp = {ctx.join("Champion")};
            MockPlayer[] other = new MockPlayer[1];
            ctx.then(() -> {
                WellRestedEffect.applyMvpToPlayer(mvp[0].get());
                ctx.check(WellRestedEffect.isMvp(mvp[0].get()), "the MVP flag is missing");
                ctx.equal(5, WellRestedEffect.getLevel(mvp[0].get()), "MVP level");
                ctx.equal(21 * 1200, WellRestedEffect.getTotalDurationTicksForPlayer(mvp[0].get()), "MVP duration");
                mvp[0].leave();
                other[0] = ctx.join("Bystander");
            }).idle(1).then(() -> {
                ctx.check(!WellRestedEffect.hasEffect(other[0].get()), "another profile got the MVP effect");
                mvp[0] = ctx.rejoin(mvp[0], ctx.at(10, 1, 4));
            }).idle(1).then(() -> {
                ctx.check(WellRestedEffect.isMvp(mvp[0].get()), "the MVP flag was lost on relog");
                ctx.check(ctx.lastSent(mvp[0], WellRestedPacket.class).mvp(), "the HUD was not told about the MVP");
            });
        }));

        mixCase(cases, true, true, "mvp_on_well_rested_on", "MVP on and Well Rested on: MVP effect for the best, normal for the rest");
        mixCase(cases, false, true, "mvp_off_well_rested_on", "MVP off and Well Rested on: nobody is MVP, everyone rests");
        mixCase(cases, true, false, "mvp_on_well_rested_off", "MVP on and Well Rested off: MVP is named but gets no effect");
        mixCase(cases, false, false, "mvp_off_well_rested_off", "MVP off and Well Rested off: no MVP and no effects");

        cases.add(new GameTestCase(ID, "switching_off_removes_effects", "Switching Well Rested off removes running effects", 100, ctx -> {
            MockPlayer player = ctx.join("Switched");
            ctx.then(() -> {
                WellRestedEffect.applyToPlayer(player.get(), 2);
                ctx.config().getWellRested().enabled = false;
            }).idle(2).then(() -> {
                ctx.check(!WellRestedEffect.hasEffect(player.get()), "the effect is still running");
                ctx.check(hasNoBonus(player.get()), "a bonus is still applied");
                ctx.equal(1, ApiEventLog.of("expired", player.uuid()).size(), "expired events fired");
            });
        }));

        cases.add(new GameTestCase(ID, "custom_attribute_added", "A custom attribute can be added to a level", 100, ctx -> {
            levelOne(ctx).attributes.add(MidnightThoughtsConfig.attributeBonus("minecraft:generic.armor", "add_value", 2.0F, 1.0F, 0.5F));
            MockPlayer player = ctx.join("Armoured");
            ctx.then(() -> {
                WellRestedEffect.applyToPlayer(player.get(), 1);
                ctx.near(2.0, bonus(player.get(), Attributes.ARMOR), "armor bonus");
            });
        }));

        cases.add(new GameTestCase(ID, "default_attribute_removed", "A default attribute can be removed from a level", 100, ctx -> {
            levelOne(ctx).attributes.removeIf(bonus -> bonus.id.equals(MidnightThoughtsConfig.ATTRIBUTE_MOVEMENT_SPEED));
            MockPlayer player = ctx.join("Slow");
            ctx.then(() -> {
                WellRestedEffect.applyToPlayer(player.get(), 1);
                ctx.check(Double.isNaN(bonus(player.get(), Attributes.MOVEMENT_SPEED)), "the removed speed bonus was applied");
                ctx.near(0.25F, bonus(player.get(), Attributes.ATTACK_DAMAGE), "the other bonuses are still applied");
            });
        }));

        cases.add(new GameTestCase(ID, "attribute_strength", "The strength of an attribute bonus is configurable", 100, ctx -> {
            for (MidnightThoughtsConfig.AttributeBonus bonus : levelOne(ctx).attributes) {
                if (bonus.id.equals(MidnightThoughtsConfig.ATTRIBUTE_ATTACK_DAMAGE)) bonus.phase1 = 5.0F;
            }
            MockPlayer player = ctx.join("Strong");
            ctx.then(() -> {
                WellRestedEffect.applyToPlayer(player.get(), 1);
                ctx.near(5.0, bonus(player.get(), Attributes.ATTACK_DAMAGE), "attack damage bonus");
            });
        }));

        cases.add(new GameTestCase(ID, "effects_added_and_tuned", "Effects can be added and their strength follows the phases", 200, ctx -> {
            MidnightThoughtsConfig.EffectBonus vision = new MidnightThoughtsConfig.EffectBonus();
            vision.id = "minecraft:night_vision";
            vision.phase1 = 0;
            vision.phase2 = 0;
            vision.phase3 = -1;
            MidnightThoughtsConfig.EffectBonus luck = new MidnightThoughtsConfig.EffectBonus();
            luck.id = "minecraft:luck";
            luck.phase1 = 2;
            luck.phase2 = 1;
            luck.phase3 = 0;
            levelOne(ctx).effects.add(vision);
            levelOne(ctx).effects.add(luck);
            MockPlayer player = ctx.join("Lucky");
            ctx.then(() -> {
                WellRestedEffect.applyToPlayer(player.get(), 1);
                ctx.equal(2, amplifier(player.get(), MobEffects.LUCK), "luck in phase 1");
                ctx.equal(0, amplifier(player.get(), MobEffects.NIGHT_VISION), "night vision in phase 1");
                setTicksLeft(player, 2401);
            }).idle(2).then(() -> {
                ctx.equal(1, amplifier(player.get(), MobEffects.LUCK), "luck in phase 2");
                setTicksLeft(player, 1201);
            }).idle(2).then(() -> {
                ctx.equal(0, amplifier(player.get(), MobEffects.LUCK), "luck in phase 3");
                ctx.equal(-1, amplifier(player.get(), MobEffects.NIGHT_VISION), "night vision switched off in phase 3");
            });
        }));

        cases.add(new GameTestCase(ID, "nothing_left_behind", "Nothing is left on the player after the effect ends", 200, ctx -> {
            levelOne(ctx).attributes.add(MidnightThoughtsConfig.attributeBonus("minecraft:generic.armor", "add_value", 2.0F, 1.0F, 0.5F));
            MidnightThoughtsConfig.EffectBonus luck = new MidnightThoughtsConfig.EffectBonus();
            luck.id = "minecraft:luck";
            luck.phase1 = 1;
            luck.phase2 = 1;
            luck.phase3 = 1;
            levelOne(ctx).effects.add(luck);
            MockPlayer player = ctx.join("Clean");
            ctx.then(() -> {
                WellRestedEffect.applyToPlayer(player.get(), 1);
                setTicksLeft(player, 1);
            }).idle(3).then(() -> {
                ctx.check(hasNoBonus(player.get()), "an attribute bonus was left behind");
                ctx.equal(-1, amplifier(player.get(), MobEffects.LUCK), "luck after the effect ended");
            });
        }));

        cases.add(new GameTestCase(ID, "unknown_ids_skipped", "Unknown attribute and effect ids are skipped safely", 100, ctx -> {
            levelOne(ctx).attributes.add(MidnightThoughtsConfig.attributeBonus("minecraft:not_a_real_attribute", "add_value", 1.0F, 1.0F, 1.0F));
            MidnightThoughtsConfig.EffectBonus broken = new MidnightThoughtsConfig.EffectBonus();
            broken.id = "nope:nothing";
            broken.phase1 = 1;
            levelOne(ctx).effects.add(broken);
            MockPlayer player = ctx.join("Careful");
            ctx.then(() -> {
                WellRestedEffect.applyToPlayer(player.get(), 1);
                ctx.check(WellRestedEffect.hasEffect(player.get()), "the effect did not start");
                ctx.near(0.08F, bonus(player.get(), Attributes.MOVEMENT_SPEED), "speed bonus next to unknown ids");
            });
        }));

        cases.add(new GameTestCase(ID, "health_and_regeneration", "Health is topped up on start and regeneration heals", 200, ctx -> {
            MockPlayer player = ctx.join("Wounded");
            float[] health = new float[1];
            ctx.then(() -> {
                player.get().setHealth(5.0F);
                WellRestedEffect.applyToPlayer(player.get(), 1);
                ctx.near(player.get().getMaxHealth(), player.get().getHealth(), "health right after the effect started");
                ctx.near(22.0, player.get().getMaxHealth(), "max health with the health bonus");
                player.get().setHealth(10.0F);
                health[0] = player.get().getHealth();
                setTicksLeft(player, 3501);
            }).idle(2).then(() -> ctx.check(player.get().getHealth() > health[0],
                    "regeneration did not heal: " + player.get().getHealth()));
        }));
    }

    private static void mixCase(List<GameTestCase> cases, boolean mvpOn, boolean restedOn, String name, String description) {
        cases.add(new GameTestCase(ID, name, description, 600, ctx -> {
            ctx.config().getMvp().enabled = mvpOn;
            ctx.config().getWellRested().enabled = restedOn;
            Bed bestBed = ctx.bed(4, 10);
            Bed otherBed = ctx.bed(16, 10);
            ctx.decorate(bestBed, Decor.LIGHTING, Decor.CARPET);
            ctx.decorate(otherBed, Decor.LIGHTING, Decor.CARPET);
            MockPlayer best = ctx.joinAt("Best", bestBed);
            MockPlayer other = ctx.joinAt("Other", otherBed);
            best.get().awardStat(Stats.BLOCK_MINED.get(Blocks.STONE), 10);
            ctx.night();
            ctx.idle(2)
                    .then(() -> {
                        ctx.sleepOrFail(best, bestBed);
                        ctx.sleepOrFail(other, otherBed);
                    })
                    .sleepThroughTheNight("the night to pass", best)
                    .then(() -> {
                        DailySummaryPacket summary = ctx.lastSummary(best);
                        ctx.equal(mvpOn, ctx.summaryLine(summary, best).isMvp(), "Best marked as MVP");
                        ctx.equal(false, ctx.summaryLine(summary, other).isMvp(), "Other marked as MVP");
                        ctx.equal(restedOn, WellRestedEffect.hasEffect(best.get()), "Best has an effect");
                        ctx.equal(restedOn, WellRestedEffect.hasEffect(other.get()), "Other has an effect");
                        if (restedOn) {
                            ctx.equal(mvpOn, WellRestedEffect.isMvp(best.get()), "Best has the MVP effect");
                            ctx.equal(mvpOn ? 5 : 2, WellRestedEffect.getLevel(best.get()), "Best level");
                            ctx.equal(2, WellRestedEffect.getLevel(other.get()), "Other level");
                            ctx.check(!WellRestedEffect.isMvp(other.get()), "Other got the MVP effect");
                        }
                    });
        }));
    }
}
