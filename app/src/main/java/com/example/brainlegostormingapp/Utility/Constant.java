package com.example.brainlegostormingapp.Utility;

public final class Constant {
    public static final int SPEED = 20;
    public static final String NOME_GRUPPO = "Brain Legostorming";
    public static final String SERVICE_ID = "com.example.brainlegostormingapp";

    public static enum OPERATION
    {
        INCORSO("in corso"),
        ANNULLATA("annullata"),
        COMPLETATA("completata");

        private final String s;

        OPERATION(final String s)
        {
            this.s = s;
        }

        @Override
        public String toString()
        {
            return s;
        }
    }
}
