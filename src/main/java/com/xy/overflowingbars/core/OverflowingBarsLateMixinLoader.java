package com.xy.overflowingbars.core;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

/**
 * Queues the mixins that target other mods' classes. MixinBooter finds implementors of
 * {@link ILateMixinLoader} through FML's ASM data at construction, once mods are on the
 * classpath; the early config cannot reach mod classes. Every target is {@code @Pseudo} with
 * {@code require = 0}, so a missing mod or a changed method is skipped.
 */
public class OverflowingBarsLateMixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.overflowingbars.late.json");
    }
}
