import central.SistemaCentral;

/**
 * Ponto de entrada simples. Limita-se a instanciar e delegar na central.
 */
public class Principal {
    public static void main(String[] args) {
        SistemaCentral central = new SistemaCentral();
        central.iniciar();
    }
}