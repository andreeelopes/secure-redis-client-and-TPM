package main.java;

/**
 * Materiais/Labs para SRSC 16/17, Sem-2
 * Henrique Domingos, 12/3/17
 **/

import java.util.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class hjCipherTest3 {
    public static void main(String args[]) {
        try {

            if (args.length != 6) {
                System.out.println("  ex: hjCipherTest topsecret AES AES/CBC/PKCS5Padding IV BC key");
                System.exit(-1);
            }
            // Primeiro vamos gerar a chave
            // a maneira de obter a chave pode ser variada:
            // podemos ter uma numa keystore ja existente ou podemos
            // simplesmente gerar uma ou podemos obte-la atraves der
            // um protocolo seguro de distribuicao de chaves

            //KeyGenerator kg = KeyGenerator.getInstance(args[1]);
            KeyGenerator keyGen = null;
            //keyGen = KeyGenerator.getInstance(args[3], args[4]);
            System.out.println(args[2]);
            System.out.println(args[4]);
            Cipher c = Cipher.getInstance(args[2], args[4]);
            byte iv[] = args[3].getBytes();
            IvParameterSpec dps = new IvParameterSpec(iv);
            String prov = args[4];

            byte[] keyBytes = args[5].getBytes();
            SecretKeySpec key = new SecretKeySpec(keyBytes, args[1]);

            //            Key key = kg.generateKey();

            System.out.println("Cifrar:");

            c.init(Cipher.ENCRYPT_MODE, key, dps);
            byte input[] = args[0].getBytes();
            byte encrypted[] = c.doFinal(input);


            byte[] encryptedBase64 = Base64.getEncoder().encode(encrypted);

            System.out.println("Ciphertext in Base64: " + new String(encryptedBase64));

            System.out.println("Decifrar: ");

            c.init(Cipher.DECRYPT_MODE, key, dps);
            byte output[] = c.doFinal(encrypted);
            System.out.println("Plaintext inicial: " + new String (output));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

