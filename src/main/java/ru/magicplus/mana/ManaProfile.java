package ru.magicplus.mana;

/** Персональные данные маны одного игрока. */
public final class ManaProfile {
    private double mana;
    private double maxMana;
    private String selectedSpell;
    private boolean wandReceived;

    public ManaProfile(double mana, double maxMana, String selectedSpell, boolean wandReceived) {
        this.maxMana = Math.max(1.0, maxMana);
        this.mana = Math.max(0.0, Math.min(mana, this.maxMana));
        this.selectedSpell = selectedSpell;
        this.wandReceived = wandReceived;
    }

    public double getMana() {
        return mana;
    }

    public void setMana(double mana) {
        this.mana = Math.max(0.0, Math.min(mana, maxMana));
    }

    public double getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(double maxMana) {
        this.maxMana = Math.max(1.0, maxMana);
        setMana(mana);
    }

    public String getSelectedSpell() {
        return selectedSpell;
    }

    public void setSelectedSpell(String selectedSpell) {
        this.selectedSpell = selectedSpell;
    }

    public boolean isWandReceived() {
        return wandReceived;
    }

    public void setWandReceived(boolean wandReceived) {
        this.wandReceived = wandReceived;
    }
}
