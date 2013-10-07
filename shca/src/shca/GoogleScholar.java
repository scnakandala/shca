package shca;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Supun Nakandala
 */
public class GoogleScholar {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        //System.setProperty("http.proxyHost", "cache.mrt.ac.lk");
        //System.setProperty("http.proxyPort", "3128");
        
        String url = "http://scholar.google.com/citations?hl=en&user=jpgplxUAAAAJ&view_op=list_works&cstart=";

        BufferedWriter writer = new BufferedWriter(new FileWriter("publications.txt"));
        writer.write("<file>");
        writer.newLine();
        for (int i = 0;; i++) {
            try {
                Document doc = Jsoup.connect(url + (i * 20))
                        .timeout(0)
                        .header("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4;"
                        + " en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
                        .userAgent("Mozilla")
                        .method(Connection.Method.GET)
                        .get();
                Elements publications = doc.select("tr.cit-table.item");
                if (publications.isEmpty()) {
                    break;
                }
                for (Element publication : publications) {
                    Element header = publication.getElementById("col-title");
                    Element title = header.getElementsByTag("a").get(0);
                    String link = "http://scholar.google.com/" + title.attr("href").replaceAll("&", "&amp;");
                    String stringTitle = title.text().replaceAll("&", "&amp;");
                    String authors = header.getElementsByTag("span").get(0).text().replaceAll("&", "&amp;");
                    String source = "";
                    if (header.getElementsByTag("span").size() > 1) {
                        source = header.getElementsByTag("span").get(1).text().replaceAll("&", "&amp;");
                    }
                    String citations = publication.getElementById("col-citedby").text();
                    String pubYear = publication.getElementById("col-year").text();

                    writer.write("<record>");
                    writer.newLine();
                    writer.write("<title>" + stringTitle + "</title>");
                    writer.newLine();
                    writer.write("<link>" + link + "</link>");
                    writer.newLine();
                    writer.write("<authors>" + authors + "</authors>");
                    writer.newLine();
                    writer.write("<source>" + source + "</source>");
                    writer.newLine();
                    writer.write("<citations>" + citations + "</citations>");
                    writer.newLine();
                    writer.write("<pub_year>" + pubYear + "</pub_year>");
                    writer.newLine();
                    writer.write("</record>");
                    writer.newLine();
                    writer.flush();
                }
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }

        }
        writer.write("</file>");
        writer.flush();
        writer.close();
    }
}
