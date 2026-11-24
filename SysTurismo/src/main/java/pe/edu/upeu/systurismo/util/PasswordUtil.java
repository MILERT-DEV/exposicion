package pe.edu.upeu.systurismo.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Investigación adicional: almacenamiento seguro de contraseñas.
 *
 * Formato guardado: pbkdf2$ITERACIONES$SAL_BASE64$HASH_BASE64
 *  - PBKDF2WithHmacSHA256 (incluido en el JDK, sin librerías externas).
 *  - Sal aleatoria por usuario: dos usuarios con la misma clave tienen hashes distintos.
 *  - Muchas iteraciones: hace lento un ataque de fuerza bruta.
 *
 * Compatibilidad: si en la base existe un hash antiguo SHA-256 (64 caracteres hex),
 * verificar() lo reconoce y el sistema lo migra al formato nuevo al iniciar sesión.
 */
public class PasswordUtil {
    private static final String PREFIJO = "pbkdf2";
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Genera el hash con sal para guardar en la base de datos. */
    public static String hash(String clave) {
        byte[] sal = new byte[BYTES_SAL];
        RANDOM.nextBytes(sal);
        byte[] h = pbkdf2(clave, sal, ITERACIONES);
        return PREFIJO + "$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(h);
    }

    /** Compara la clave escrita con el hash guardado (formato nuevo o SHA-256 antiguo). */
    public static boolean verificar(String clave, String guardado) {
        if (clave == null || guardado == null) return false;
        if (guardado.startsWith(PREFIJO + "$")) {
            String[] p = guardado.split("\\$");
            if (p.length != 4) return false;
            try {
                int iter = Integer.parseInt(p[1]);
                byte[] sal = Base64.getDecoder().decode(p[2]);
                byte[] esperado = Base64.getDecoder().decode(p[3]);
                return MessageDigest.isEqual(esperado, pbkdf2(clave, sal, iter)); // comparación en tiempo constante
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return MessageDigest.isEqual(guardado.getBytes(StandardCharsets.UTF_8),
                sha256(clave).getBytes(StandardCharsets.UTF_8));
    }

    /** true si el hash guardado es del formato antiguo y conviene migrarlo. */
    public static boolean esAntiguo(String guardado) {
        return guardado != null && !guardado.startsWith(PREFIJO + "$");
    }

    private static byte[] pbkdf2(String clave, byte[] sal, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, BITS_HASH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo calcular el hash de la contraseña", e);
        }
    }

    private static String sha256(String clave) {
        try {
            byte[] b = MessageDigest.getInstance("SHA-256").digest(clave.getBytes(StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            for (byte x : b) s.append(String.format("%02x", x));
            return s.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
