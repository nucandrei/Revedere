package org.nuc.revedere.util;

public class CustomCharacter {
    private char character;

    public CustomCharacter(char character) {
        this.character = character;
    }

    /**
     * Increment the character value
     * @return true if overflow was reached, false otherwise
     */
    public boolean increment() {
        this.character++;
        if (this.character > '~') {
            this.character = '!';
            return true;
        }
        return false;
    }
    
    public char getCharacter() {
        return this.character;
    }
}
