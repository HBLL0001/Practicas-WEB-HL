package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;

import java.io.File;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClients;


public class Main {
    private static final List<String> formResults = new ArrayList<>();
    public static void main(String[] args) {
        System.out.println("ClientE Http, inserta URL:");
        String URL = new Scanner(System.in).nextLine();

        try(HttpClient httpClient = HttpClient.newHttpClient()){
            HttpRequest request =  HttpRequest.newBuilder().uri(new URI(URL)).build();
            HttpResponse<String> answer = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            HttpHeaders headerType = answer.headers();


            if (headerType.firstValue("Tipo de Contenido").orElse("").contains("text/html")) {
                System.out.println("Este archivo es HTML");

                String document = answer.body();
                Document doc = Jsoup.parse(document);

                //Lineas Cant
                System.out.println("Cantidad de Lineas: "+ quantLine(document) );
                System.out.println();

                //Parrafo Cant
                Elements p =doc.select("p");
                System.out.println("Cantidad de Parrafos: " +p.size());

                //Img Cant
                Elements images = doc.select("img");
                System.out.println("Cantidad de Imagenes: " + images.size());
                System.out.println();

                //Forms Cant
                Elements forms = doc.select("form");
                System.out.println("Cantidad de Forms: "+ forms.size());
                // System.out.println("(POST) Form:"+ countForm(forms, "post"));
                //System.out.println("(GET) Form:"+ countForm(forms, "get"));
                countForm(forms);
                System.out.println();

                //for every form show the fields of input
                System.out.println("Forms field input and value");
                for(int i=0; i<forms.size();i++){
                    org.jsoup.nodes.Element form =forms.get(i);
                    System.out.println("Form "+ String.format("%0"+3+"d",i));
                    Elements inputs = form.select("input");
                    for(org.jsoup.nodes.Element input: inputs){
                        //System.out.println("Type:"+ input.attr("type"));
                        String inputType = input.attr("type");
                        String inputValue =input.attr("Value");
                        System.out.println("Type: "+ inputType + ", Value: "+inputValue);
                    }
                }
                System.out.println();

                List<String> localResult = new ArrayList<>();

                System.out.println("Generando peticion");
                for (int i = 0; i < forms.size(); i++) {
                    org.jsoup.nodes.Element form = forms.get(i);
                    if (form.attr("method").equalsIgnoreCase("post")) {
                        System.out.println("Form: " + String.format("%03d", i));
                        HttpPost httpPost = new HttpPost(form.absUrl("action"));
                        httpPost.addHeader("Matricula-ID", "1014-5127");
                        List<BasicNameValuePair> parameters = new ArrayList<>();
                        parameters.add(new BasicNameValuePair("Asignatura", "Practica-1"));
                        httpPost.setEntity(new UrlEncodedFormEntity(parameters));
                        try (CloseableHttpClient client = HttpClients.createDefault();
                             CloseableHttpResponse response = client.execute(httpPost)) {
                            int statusCode= response.getStatusLine().getStatusCode();
                            localResult.add("From "+ String.format("%03d",i)+": Status code:"+statusCode);
                            // System.out.println("Status code: " + response.getStatusLine().getStatusCode());
                        }
                    }
                }
                formResults.addAll(localResult);
            } else if(headerType.firstValue("Content-Type").orElse("").contains("application/pdf")){
                System.out.println("Este archivo es un PDF");
                System.out.println("Por favor pega el path del PDF");
                String pdfFilepath = new Scanner(System.in).nextLine();
                readPDF(pdfFilepath);
            }else {
                System.out.println("Este archivo es: " + headerType.firstValue("Tipo de Contenido").orElse("") + " ");
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
            formResults.add("Error"+e.getMessage());

        }finally {
            System.out.println("Resultado:");
            for (String result :formResults){
                System.out.println(result);
            }
        }
    }

    private static int quantLine(String document) {
        int quatnline = 0;
        for (int i = 0; i < document.length(); i++) {
            if (String.valueOf(document.charAt(i)).equals("\n")) {
                quatnline++;
            }
        }
        return quatnline;
    }

    private  static void  countForm(Elements forms){
        int countpost =0;
        int countget =0;

        for (org.jsoup.nodes.Element form :forms){
            String formMethod = form.attr("metodo").toLowerCase();
            if (formMethod.equals("post")){
                countpost++;
            }else  if (formMethod.equals("get")){
                countget++;
            }
        }
        System.out.println("(POST) Form:"+countpost);
        System.out.println("(GET)  Form:"+countget);
    }
    private static void  readPDF(String filePath){
        try(PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            String pdfText = pdfTextStripper.getText(document);

            System.out.println("Contenido del PDF:\n" + pdfText);
        }catch (IOException e){
            e.printStackTrace();
            formResults.add("Error leeyendo PDF:"+e.getMessage());

        }
    }
}