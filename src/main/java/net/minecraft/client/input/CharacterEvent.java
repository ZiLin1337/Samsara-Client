package net.minecraft.client.input;

import net.minecraft.SharedConstants;

public record CharacterEvent(char character) {
    public boolean isAllowedChatCharacter() {
        return SharedConstants.isAllowedChatCharacter(this.character);
    }

    public String codepointAsString() {
        return String.valueOf(this.character);
    }
}
