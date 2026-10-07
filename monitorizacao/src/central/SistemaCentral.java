package central;

import registo.RegistoCentral;
import sensores.Sensor;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SistemaCentral {
    private final RegistoCentral registo = new RegistoCentral();
    private final List<Thread> threadsSensores = new ArrayList<>();
    private long tempoInicio;

    public void iniciar() {
        configurarSensores();
        tempoInicio = System.currentTimeMillis();
        
        System.out.println("=== CENTRAL DE MONITORIZAÇÃO ATIVA ===");
        System.out.println("Opções: [1] Últimas leituras | [2] Estatísticas parciais | [0] Encerrar");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                if (!scanner.hasNextLine()) break; // Trata EOF como pedido de encerramento (Regra 8)
                
                String opcao = scanner.nextLine().trim();
                if (opcao.equals("0")) {
                    break;
                } else if (opcao.equals("1")) {
                    registo.imprimirUltimasLeituras();
                } else if (opcao.equals("2")) {
                    registo.imprimirEstatisticasParciais();
                } else {
                    System.out.println("Opção inválida.");
                }
            } catch (Exception e) {
                break;
            }
        }
        encerrarCoordenado();
    }

    private void configurarSensores() {
        threadsSensores.add(new Thread(new Sensor("TEMP-01", "°C", 18.0, 27.0, 500, registo), "Sensor-TEMP-01"));
        threadsSensores.add(new Thread(new Sensor("HUM-01", "%", 40.0, 60.0, 800, registo), "Sensor-HUM-01"));
        threadsSensores.add(new Thread(new Sensor("PRES-01", "hPa", 1000.0, 1025.0, 1000, registo), "Sensor-PRES-01"));

        for (Thread t : threadsSensores) {
            t.start(); // Arranque exclusivo com start()
        }
    }

    private void encerrarCoordenado() {
        System.out.println("\nEncerramento coordenado iniciado...");
        long inicioEncerramento = System.currentTimeMillis();

        // 1. Sinalizar paragem cooperativa via interrupt()
        for (Thread t : threadsSensores) {
            t.interrupt();
        }

        // 2. Coordenar fim absoluto de todas as threads via join()
        for (Thread t : threadsSensores) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        long duracaoEncerramento = System.currentTimeMillis() - inicioEncerramento;
        long duracaoMonitorizacao = System.currentTimeMillis() - tempoInicio;

        // 3. Gerar relatório apenas após o encerramento absoluto (isAlive() == false)
        registo.imprimirRelatorioFinal(duracaoMonitorizacao, duracaoEncerramento);
        
        System.out.println("Estado final das threads:");
        boolean todasTerminadas = true;
        for (Thread t : threadsSensores) {
            System.out.println(" " + t.getName() + " " + t.getState());
            if (t.getState() != Thread.State.TERMINATED) {
                todasTerminadas = false;
            }
        }
        System.out.println("Todas as threads terminadas : " + (todasTerminadas ? "SIM" : "NÃO"));
        System.out.println("================================================================");
    }
}