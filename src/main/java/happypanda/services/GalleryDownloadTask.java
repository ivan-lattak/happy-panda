package happypanda.services;

import javafx.concurrent.Task;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GalleryDownloadTask extends Task<Void> {

    private static final int IMAGES_PER_PAGE = 20;

    @NotNull
    private static final String COOKIE_KEY_IPB_MEMBER_ID = "ipb_member_id";
    @NotNull
    private static final String COOKIE_KEY_IPB_PASS_HASH = "ipb_pass_hash";

    @NotNull
    private static final String SELECTOR_IMAGE_LINKS_FROM_GALLERY_PAGE = "body > div#gdt > div.gdtl > a";
    @NotNull
    private static final String SELECTOR_NUMBER_OF_IMAGES_FROM_BASE_PAGE = "body > div.gtb > p.gpc";

    @NotNull
    private static final Pattern SUMMARY_PATTERN = Pattern.compile("Showing \\d+ - \\d+ of (\\d+) images");

    @NotNull
    private final String baseGalleryUrl;
    @NotNull
    private final String ipbMemberId;
    @NotNull
    private final String ipbPassHash;
    @NotNull
    private final Path outputFolder;

    public GalleryDownloadTask(@NotNull String baseGalleryUrl,
                               @NotNull String ipbMemberId,
                               @NotNull String ipbPassHash,
                               @NotNull Path outputFolder) {
        this.baseGalleryUrl = baseGalleryUrl;
        this.ipbMemberId = ipbMemberId;
        this.ipbPassHash = ipbPassHash;
        this.outputFolder = outputFolder;
    }

    @Override
    @Nullable
    protected Void call() throws IOException {
        for (String pageUrl : galleryPageUrls()) {
            Elements elements = getPage(pageUrl).select(SELECTOR_IMAGE_LINKS_FROM_GALLERY_PAGE);
            System.out.println(elements);
        }
        return null;
    }

    @NotNull
    private Iterable<String> galleryPageUrls() throws IOException {
        Document document = getPage(baseGalleryUrl);
        int numberOfPages = getNumberOfPages(extractNumberOfImages(document));
        return () -> new SadPandaUrlGenerator(baseGalleryUrl, numberOfPages);
    }

    @Contract(pure = true)
    private static int getNumberOfPages(int images) {
        int pages = images / IMAGES_PER_PAGE;
        if (images % IMAGES_PER_PAGE != 0) {
            pages++;
        }
        return pages;
    }

    @Contract(pure = true)
    private static int extractNumberOfImages(@NotNull Document document) {
        String text = document.select(SELECTOR_NUMBER_OF_IMAGES_FROM_BASE_PAGE).text();
        Matcher matcher = SUMMARY_PATTERN.matcher(text);
        if (!matcher.matches()) {
            throw new SiteFormatException(); // if this is thrown, then the impossible happened and the site was changed
        }

        return Integer.valueOf(matcher.group(1));
    }

    @NotNull
    private Document getPage(@NotNull String pageUrl) throws IOException {
        try {
            return Jsoup.connect(pageUrl)
                    .cookie(COOKIE_KEY_IPB_MEMBER_ID, ipbMemberId)
                    .cookie(COOKIE_KEY_IPB_PASS_HASH, ipbPassHash)
                    .get();
        } catch (UnsupportedMimeTypeException e) {
            System.out.println();
            throw new SadPandaException(e);
        }
    }

    private static class SadPandaUrlGenerator implements Iterator<String> {

        @NotNull
        private final String baseUrl;
        private final int numberOfPages;

        private int pagesGenerated = 0;

        private SadPandaUrlGenerator(@NotNull String baseUrl, int numberOfPages) {
            this.baseUrl = baseUrl;
            this.numberOfPages = numberOfPages;
        }

        @Override
        public boolean hasNext() {
            return pagesGenerated < numberOfPages;
        }

        @Override
        @NotNull
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return baseUrl + "?p=" + (pagesGenerated++);
        }

    }

}
