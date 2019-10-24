package com.example.myfirstapp;

import android.content.Context;
import android.os.SystemClock;

public class Cronometro implements Runnable
{
    public static final long MINUTO = 60000;
    public static final long ORA = 3600000;

    private Context contesto;
    private long tempoInizio;
    private boolean conta;

    public Cronometro(Context contesto)
    {
        this.contesto = contesto;
    }

    public void start()
    {
        tempoInizio = System.currentTimeMillis();
        conta=true;
    }

    public void stop()
    {
        conta=false;
    }

    @Override
    public void run()
    {
        while(conta)
        {
            long attuale = System.currentTimeMillis() - tempoInizio;

            int secondi = (int) (attuale/1000) % 60;
            int minuti = (int) (attuale/MINUTO) % 60;
            int ore = (int) (attuale/ORA) % 24;
            int millisecondi = (int) attuale % 1000;

            ((MainActivity)contesto).aggiornaTimer(String.format("%02d:%02d:%02d:%03d", ore, minuti, secondi, millisecondi));
        }
    }
}
