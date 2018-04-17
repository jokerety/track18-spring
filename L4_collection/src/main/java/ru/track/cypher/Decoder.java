package ru.track.cypher;

import java.util.*;

import org.jetbrains.annotations.NotNull;

public class Decoder {

    // Расстояние между A-Z -> a-z
    public static final int SYMBOL_DIST = 32;

    private Map<Character, Character> cypher;

    /**
     * Конструктор строит гистограммы открытого домена и зашифрованного домена
     * Сортирует буквы в соответствие с их частотой и создает обратный шифр Map<Character, Character>
     *
     * @param domain - текст по кторому строим гистограмму языка
     */
    public Decoder(@NotNull String domain, @NotNull String encryptedDomain) {
        Map<Character, Integer> domainHist = createHist(domain);
        Map<Character, Integer> encryptedDomainHist = createHist(encryptedDomain);


        cypher = new HashMap<>();

        Iterator<Map.Entry<Character, Integer>> itr1 = encryptedDomainHist.entrySet().iterator();
        Iterator<Map.Entry<Character, Integer>> itr2 = domainHist.entrySet().iterator();

        while (itr1.hasNext()) {
            Map.Entry<Character, Integer> entryOriginal = itr1.next();
            Map.Entry<Character, Integer> openKey = itr2.next();
            cypher.put(entryOriginal.getKey(), openKey.getKey());
        }
    }

    public Map<Character, Character> getCypher() {
        return cypher;
    }

    /**
     * Применяет построенный шифр для расшифровки текста
     *
     * @param encoded зашифрованный текст
     * @return расшифровка
     */
    @NotNull
    public String decode(@NotNull String encoded) {
        StringBuilder strBuilder = new StringBuilder(encoded.length());

        for (int i = 0; i < encoded.length(); i++) {
            if (Character.isLetter(encoded.charAt(i))) {
                strBuilder.append(cypher.get(encoded.charAt(i)));
            } else {
                strBuilder.append(encoded.charAt(i));

            }
        }
        return strBuilder.toString();
    }

    /**
     * Считывает входной текст посимвольно, буквы сохраняет в мапу.
     * Большие буквы приводит к маленьким
     *
     * @param text - входной текст
     * @return - мапа с частотой вхождения каждой буквы (Ключ - буква в нижнем регистре)
     * Мапа отсортирована по частоте. При итерировании на первой позиции наиболее частая буква
     */
    @NotNull
    Map<Character, Integer> createHist(@NotNull String text) {
        Map<Character, Integer> result = new LinkedHashMap<>();

        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetter(text.charAt(i))) {
                if (result.containsKey(Character.toLowerCase(text.charAt(i)))) {
                    result.put(Character.toLowerCase(text.charAt(i)), result.get(Character.toLowerCase(text.charAt(i))) + 1);
                } else {
                    result.put(Character.toLowerCase(text.charAt(i)), 1);
                }
            }
        }
        List<Map.Entry<Character, Integer>> listec = new ArrayList<>(result.entrySet());

        listec.sort((o1, o2) -> {
            return o2.getValue() - o1.getValue();
        });

        Map<Character, Integer> nowRealResult = new LinkedHashMap<>();
        for (int i = 0; i < listec.size(); i++) {
            nowRealResult.put(listec.get(i).getKey(), listec.get(i).getValue());
        }
        return nowRealResult;
    }

}
