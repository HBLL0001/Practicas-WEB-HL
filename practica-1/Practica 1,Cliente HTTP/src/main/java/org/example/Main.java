package org.example;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.tools.DocumentationTool;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Cliente HTTP 1014-5127");
        System.out.println("Pegue el URL:");

        Scanner input = new Scanner(System.in);
        String link = input.nextLine();

        if(displayContenido(link)){
            contarLineas(link);
            contarParagphs(link);
            contarForms(link);
            printForms(link);
            sendForms(link);
        }
    }

    //se conecta al link determina su tipo de documento (como HTML)
    // y devuelve true si el tipo de documento es HTML, o false en caso contrario.
    // Si el tipo de documento no es compatible o si hay un error durante el proceso,
    // devuelve false e imprime un mensaje apropiado.
    private static boolean displayContenido(String link){
        try{
            Document documento = Jsoup.connect(link).get();
            String tipo = documento.documentType().toString();
            System.out.printf("\nEste recurso es: %s\n", tipo);
            return tipo.equals("<!doctype html>");
        }catch (UnsupportedMimeTypeException x){
            System.out.printf("Tipo de recurso: %s\n", x.getMimeType());
            return false;
        }catch (IOException x){
            throw new RuntimeException(x);
        }
    }

    //Este método cuenta el número de líneas en el contenido HTML obtenido de una URL.
    // Conecta a la URL especificada, obtiene el contenido HTML, lo divide en líneas utilizando el carácter de nueva línea (\n),
    // y luego imprime el número total de líneas obtenidas. Si hay algún error durante el proceso de conexión o obtención del contenido,
    // se lanza una excepción IOException.
    public static void contarLineas(String link) throws IOException {
        Document doc = Jsoup.connect(link).get();
        String[] lines = doc.html().split("\n");
        int numberOfLines = lines.length;
        System.out.println("Numero de lineas " + numberOfLines);
    }

    //Este método cuenta el número de párrafos y el número total de imágenes dentro de esos párrafos en el contenido .
    // Se conecta a la URL, selecciona todos los elementos de párrafo (<p>) cuenta su cantidad y luego itera sobre cada párrafo para contar las imágenes (<img>) .
    // Finalmente, imprime los números de párrafos e imágenes. Si hay algún error durante el proceso de conexión o obtención del contenido, se lanza una excepción IOException.
    public static void contarParagphs(String link) throws IOException {
        Document doc = Jsoup.connect(link).get();
        Elements paragraphs = doc.select("p");
        int numberOfParagraphs = paragraphs.size();
        System.out.println("Número de párrafos: " + numberOfParagraphs);

        int numberOfImagesInParagraphs = 0;
        for (Element p : paragraphs) {
            numberOfImagesInParagraphs += p.select("img").size();
        }
        System.out.println("Número de imágenes dentro de los párrafos: " + numberOfImagesInParagraphs);
    }

    //Este método cuenta el número de formularios (elementos HTML <form>) en un documento HTML obtenido.
    // Luego, clasifica los formularios según sus métodos de envío HTTP (GET o POST), contando cuántos usan cada método.
    // Finalmente, imprime el recuento de formularios que utilizan GET y POST respectivamente.
    public static void contarForms(String link) throws IOException {
        Document document = Jsoup.connect(link).get();
        Elements forms = document.select("form");
        int getCount = 0;
        int postCount = 0;

        for (Element form : forms) {
            String method = form.attr("method").toUpperCase();

            switch (method) {
                case "GET":
                    getCount++;
                    break;
                case "POST":
                    postCount++;
                    break;
                default:
                    // Para otros metodos si es necesario.
                    break;
            }
        }

        System.out.println("Número de formularios con método GET: " + getCount);
        System.out.println("Número de formularios con método POST: " + postCount);
    }

    //Este método obtiene y muestra los formularios.
    // Itera sobre cada formulario, identificando y mostrando los elementos de entrada (input) .
    // Para cada input, se muestra su tipo de entrada (tipo) y se numeran según el orden en que aparecen en el formulario.
    public static void printForms(String link) throws IOException {
        Document document = Jsoup.connect(link).get();
        Elements forms = document.select("form");
        int cont = 0;
        for (int i = 0; i < forms.size(); i++) {
            Element form = forms.get(i);
            cont++;
            System.out.printf("\nInputs del form #%d:\n", cont);
            Elements inputs = form.select("input");
            int j = 0;
            while (j < inputs.size()) {
                Element input = inputs.get(j);
                String type = input.attr("tipo");
                System.out.println("\tInput " + input);
                System.out.println("\t\tTipo: " + type);
                j++;
            }
        }
    }
    //Si encuentra un formulario con un método de envío "POST", envía una solicitud POST a la URL especificada en el atributo "action" del formulario,
    // proporcionando datos y encabezados personalizados.
    // Finalmente, imprime el código de estado de la respuesta HTTP obtenida.
    public static void sendForms(String link) throws IOException{
        Document documento = Jsoup.connect(link).get();
        Elements forms = documento.select("Form");
        for(Element form : forms){
            String method = form.attr("method").toUpperCase();
            if(method.equals("POST")){
                String action = form.attr("action");
                Connection.Response resp = Jsoup.connect(action)
                        .data("Asignatura", "Practica 1 Cliente HTTP")
                        .header("Matricula","1014-5127")
                        .method(Connection.Method.POST)
                        .execute();
                System.out.println("Status:" + resp.statusCode());
            }
        }
    }


}