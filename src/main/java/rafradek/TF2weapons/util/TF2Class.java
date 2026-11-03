package rafradek.TF2weapons.util;

import rafradek.TF2weapons.entity.mercenary.*;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import java.util.function.Predicate;

public enum TF2Class {
    SCOUT("scout", EntityScout.class),
    SOLDIER("soldier", EntitySoldier.class),
    PYRO("pyro", EntityPyro.class),
    DEMOMAN("demoman", EntityDemoman.class),
    HEAVY("heavy", EntityHeavy.class),
    ENGINEER("engineer", EntityEngineer.class),
    MEDIC("medic", EntityMedic.class),
    SNIPER("sniper", EntitySniper.class),

    SPY("spy", EntitySpy.class),
    NONE("", null);

    private static Random random = new Random();

    protected final String name;
    protected final Class<? extends EntityTF2Character> clazz;

    TF2Class(String name, Class<? extends EntityTF2Character> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return clazz == null ? -1 : ordinal();
    }

    public MutableComponent getLocalizedName() {
        return Component.translatable("entity." + name + ".name");
    }

    public Class<? extends EntityTF2Character> getEntityClass() {
        return clazz;
    }

    public EntityTF2Character createEntity(Level world) {
        try {
            return clazz.getConstructor(Level.class).newInstance(world);
        } catch (Exception e) {
            return null;
        }
    }

    public static TF2Class getRandomClass() {
        return values()[random.nextInt(values().length - 1)];
    }

    public static TF2Class getRandomClass(Predicate<TF2Class> predicate) {
        TF2Class tfClass = getRandomClass();
        while (!predicate.test(tfClass)) tfClass = getRandomClass();
        return tfClass;
    }

    public static TF2Class getClass(int id) {
        if (id < 0 || id > values().length) id = 9;
        return values()[id];
    }

    public static TF2Class getClass(String name) {
        for (TF2Class clazz : values()) if (name.equals(clazz.name.toLowerCase(Locale.US))) return clazz;
        return NONE;
    }

    public static List<TF2Class> getClasses() {
        return  Arrays.stream(values()).filter(clazz -> clazz.getIndex() >= 0).collect(Collectors.toList());
    }

    public static String[] getClassNames() {
        return getClasses().stream().map(TF2Class::getName).collect(Collectors.toList()).toArray(new String[]{});
    }

}
