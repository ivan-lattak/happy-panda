package happypanda;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HappyPandaController {

    private static final int IMAGES_PER_PAGE = 20;

    private static final String COOKIE_KEY_IPB_MEMBER_ID = "ipb_member_id";
    private static final String COOKIE_KEY_IPB_PASS_HASH = "ipb_pass_hash";

    private static final String SELECTOR_IMAGE_LINKS_FROM_GALLERY_PAGE = "body > div#gdt > div.gdtl > a";
    private static final String SELECTOR_NUMBER_OF_IMAGES_FROM_BASE_PAGE = "body > div.gtb > p.gpc";

    private static final Pattern SUMMARY_PATTERN = Pattern.compile("Showing \\d+ - \\d+ of (\\d+) images");

    private final String SAD_PANDA_TEST_URL = "https://exhentai.org/g/1307435/35d8bdd1b1/";

    public void handleDownload() throws IOException {
        for (String pageUrl : galleryPageUrls()) {
            Elements elements = getGalleryPage(pageUrl).select(SELECTOR_IMAGE_LINKS_FROM_GALLERY_PAGE);
            System.out.println(elements);
        }
    }

    private @NotNull Iterable<String> galleryPageUrls() throws IOException {
        Document document = getGalleryPage(SAD_PANDA_TEST_URL);
        int numberOfPages = getNumberOfPages(extractNumberOfImages(document));
        return () -> new SadPandaUrlGenerator(SAD_PANDA_TEST_URL, numberOfPages);
    }

    private static int getNumberOfPages(int images) {
        int pages = images / IMAGES_PER_PAGE;
        if (images % IMAGES_PER_PAGE != 0) {
            pages++;
        }
        return pages;
    }

    private static int extractNumberOfImages(Document document) {
        String text = document.select(SELECTOR_NUMBER_OF_IMAGES_FROM_BASE_PAGE).text();
        Matcher matcher = SUMMARY_PATTERN.matcher(text);
        if (!matcher.matches()) {
            throw new Error(); // if this is thrown, then the impossible happened and the site was changed
        }

        return Integer.valueOf(matcher.group(1));
    }

    private static Document getGalleryPage(@NotNull String galleryPageUrl) throws IOException {
        return Jsoup.connect(galleryPageUrl)
                .cookie(COOKIE_KEY_IPB_MEMBER_ID, "")
                .cookie(COOKIE_KEY_IPB_PASS_HASH, "")
                .get();
    }

    private static class SadPandaUrlGenerator implements Iterator<String> {

        private final String baseUrl;
        private final int numberOfPages;

        private int pagesGenerated = 0;

        private SadPandaUrlGenerator(String baseUrl, int numberOfPages) {
            this.baseUrl = baseUrl;
            this.numberOfPages = numberOfPages;
        }

        @Override
        public boolean hasNext() {
            return pagesGenerated < numberOfPages;
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return baseUrl + "?p=" + (pagesGenerated++);
        }

    }

}
