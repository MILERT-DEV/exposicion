package pe.edu.upeu.systurismo.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pe.edu.upeu.systurismo.dto.PersonaDto;

import java.io.IOException;

/**
 * Consulta de datos por DNI en eldni.com (web scraping con Jsoup).
 * Es un servicio de terceros: si el sitio cambia o no responde, el registro manual sigue disponible.
 */
public class ConsultaDNI {
    private static final String URL = "https://eldni.com/pe/buscar-datos-por-dni";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) SysTurismo";
    private static final int TIMEOUT_MS = 10_000;

    /** @throws IOException si no hay conexión o el sitio no responde. */
    public PersonaDto consultarDNI(String dni) throws IOException {
        PersonaDto personaDto = new PersonaDto();

        // Primero: GET para obtener el _token y la sesión
        Connection.Response getResponse = Jsoup.connect(URL)
                .userAgent(USER_AGENT).timeout(TIMEOUT_MS)
                .method(Connection.Method.GET)
                .execute();
        Document doc = getResponse.parse();
        String token = doc.select("input[name=_token]").attr("value");

        // Segundo: POST con el DNI y el token
        Connection.Response postResponse = Jsoup.connect(URL)
                .userAgent(USER_AGENT).timeout(TIMEOUT_MS)
                .cookies(getResponse.cookies())
                .data("_token", token)
                .data("dni", dni)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();

        // Tercero: leer la primera fila de la tabla de resultados
        Document resultDoc = Jsoup.parse(postResponse.body());
        Element fila = resultDoc.select("table tbody tr").first();
        if (fila != null) {
            Elements celdas = fila.select("td");
            if (celdas.size() >= 4) {
                personaDto.setDni(celdas.get(0).text());
                personaDto.setNombre(celdas.get(1).text());
                personaDto.setApellidoPaterno(celdas.get(2).text());
                personaDto.setApellidoMaterno(celdas.get(3).text());
            }
        }
        return personaDto;
    }
}
