package sensores;

import registo.Leitura;
import registo.RegistoCentral;
import java.util.Random;

/**
 * Tarefa de leitura implementando Runnable para desacolamento da Thread.
 */
public class Sensor implements Runnable {
    private final String nome;
    private final String unidade;
    private final double minVal;
    private final double maxVal;
    private final int intervaloBase;
    private final RegistoCentral registo;
    private int leiturasEfetuadas = 0;

    public Sensor(String nome, String unidade, double minVal, double maxVal, int intervaloBase, RegistoCentral registo) {
        this.nome = nome;
        this.unidade = unidade;
        this.minVal = minVal;
        this.maxVal = maxVal;
        this.intervaloBase = intervaloBase;
        this.registo = registo;
    }

    @Override
    public void run() {
        Random random = new Random();
        int jitter = (int) (intervaloBase * 0.2);

        // Verificação cooperativa do estado de interrupção
        while (!Thread.currentThread().isInterrupted()) {
            double valor = minVal + (maxVal - minVal) * random.nextDouble();
            Leitura leitura = new Leitura(nome, valor, unidade, System.currentTimeMillis());
            
            registo.registar(leitura);
            leiturasEfetuadas++;
            
            // Pausa simulada (intervalo base ± 20%) fora do bloco synchronized
            int pause = intervaloBase - jitter + random.nextInt(2 * jitter + 1);
            try {
                Thread.sleep(pause);
            } catch (InterruptedException e) {
                // TRATAMENTO OBRIGATÓRIO: Restaurar a flag de interrupção e sair do ciclo ordeiramente
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("[" + nome + "] terminado de forma controlada (" + leiturasEfetuadas + " leituras).");
    }
}