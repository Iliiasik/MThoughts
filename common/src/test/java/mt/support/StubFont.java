package mt.support;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.glyphs.EffectGlyph;
import net.minecraft.network.chat.FontDescription;

import java.util.function.ToIntFunction;

public final class StubFont extends Font {

    private final ToIntFunction<String> widths;

    public StubFont(ToIntFunction<String> widths) {
        super(new Font.Provider() {
            @Override
            public GlyphSource glyphs(FontDescription font) {
                throw new UnsupportedOperationException();
            }

            @Override
            public EffectGlyph effect() {
                throw new UnsupportedOperationException();
            }
        });
        this.widths = widths;
    }

    public static StubFont fixedWidth(int perCharacter) {
        return new StubFont(text -> text.length() * perCharacter);
    }

    @Override
    public int width(String str) {
        return str == null ? 0 : widths.applyAsInt(str);
    }
}
