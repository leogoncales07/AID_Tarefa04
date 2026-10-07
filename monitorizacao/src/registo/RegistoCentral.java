package registo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Único recurso partilhado. Todos os métodos de acesso são synchronized (intrinsic lock).
 */
public class RegistoCentral {
    private final List<Leitura> historico = new ArrayList<>();
    private int contadorTotal = 0;
    private final Map<String, Estatistica> estatisticas = new HashMap<>();
    private final Map<String, Leitura> ultimasLeituras = new HashMap<>();

    private static class Estatistica {
        int contagem = 0;
        double minimo = Double.MAX_VALUE;
        double maximo = -Double.MAX_VALUE;
        double soma = 0;
        String unidade = "";
    }

    // Operação composta, garantida como atómica
    public synchronized void registar(Leitura leitura) {
        historico.add(leitura);
        contadorTotal++;
        ultimasLeituras.put(leitura.getSensor(), leitura);

        Estatistica stat = estatisticas.computeIfAbsent(leitura.getSensor(), k -> new Estatistica());
        stat.contagem++;
        stat.soma += leitura.getValor();
        stat.unidade = leitura.getUnidade();
        if (leitura.getValor() < stat.minimo) stat.minimo = leitura.getValor();
        if (leitura.getValor() > stat.maximo) stat.maximo = leitura.getValor();
    }

    public synchronized void imprimirUltimasLeituras() {
        System.out.println("\n--- ÚLTIMAS LEITURAS ---");
        for (Leitura l : ultimasLeituras.values()) {
            System.out.printf("[%s] %.2f %s%n", l.getSensor(), l.getValor(), l.getUnidade());
        }
        System.out.println("------------------------");
    }

    public synchronized void imprimirEstatisticasParciais() {
        System.out.println("\n--- ESTATÍSTICAS PARCIAIS ---");
        for (Map.Entry<String, Estatistica> entry : estatisticas.entrySet()) {
            Estatistica s = entry.getValue();
            double media = s.contagem > 0 ? s.soma / s.contagem : 0;
            System.out.printf("[%s] Leituras: %d | Min: %.2f | Max: %.2f | Média: %.2f %s%n",
                    entry.getKey(), s.contagem, s.minimo, s.maximo, media, s.unidade);
        }
        System.out.println("-----------------------------");
    }

    public synchronized boolean verificarConsistencia() {
        int somaContagens = 0;
        for (Estatistica s : estatisticas.values()) {
            somaContagens += s.contagem;
        }
        return (contadorTotal == historico.size()) && (contadorTotal == somaContagens);
    }

    public synchronized void imprimirRelatorioFinal(long duracaoMonitorizacao, long duracaoEncerramento) {
        System.out.println("================================================================");
        System.out.println("RELATÓRIO FINAL DE MONITORIZAÇÃO");
        System.out.println("================================================================");
        System.out.printf("Duração da monitorização   : %.1f s%n", duracaoMonitorizacao / 1000.0);
        System.out.printf("Duração do encerramento    : %d ms%n", duracaoEncerramento);
        System.out.println("----------------------------------------------------------------");
        System.out.println("Sensor       Leituras  Mínimo  Máximo  Média  Unidade");
        for (Map.Entry<String, Estatistica> entry : estatisticas.entrySet()) {
            Estatistica s = entry.getValue();
            double media = s.contagem > 0 ? s.soma / s.contagem : 0;
            System.out.printf("%-12s %-9d %-7.1f %-7.1f %-6.1f %s%n",
                    entry.getKey(), s.contagem, s.minimo, s.maximo, media, s.unidade);
        }
        System.out.println("----------------------------------------------------------------");
        
        int somaContagens = estatisticas.values().stream().mapToInt(e -> e.contagem).sum();
        System.out.println("Total de leituras (contador) : " + contadorTotal);
        System.out.println("Soma das leituras por sensor : " + somaContagens);
        System.out.println("Registos no histórico        : " + historico.size());
        System.out.println("Verificação de consistência  : " + (verificarConsistencia() ? "OK" : "FALHA"));
    }
}