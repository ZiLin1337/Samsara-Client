package net.minecraft.client.input;

public record CharacterEvent(char character) {
    public boolean isAllowedChatCharacter() {
        // Simplified check - allow most printable characters
        return character >= 32 && character < 127;
    }

    public String codepointAsString() {
        return String.valueOf(this.character);
    }
}
