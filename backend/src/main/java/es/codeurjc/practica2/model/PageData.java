package es.codeurjc.practica2.model;
 
import java.util.ArrayList;
import java.util.List;
 
/**
 * Generic pagination wrapper that Mustache templates can render directly.
 * Holds the page content plus all metadata needed to draw a pagination bar.
 */
public class PageData<T> {
 
    private final List<T> content;
    private final int currentPage;
    private final int totalPages;
    private final long totalElements;
    private final int pageSize;
 
    public PageData(List<T> content, int currentPage, int totalPages,
            long totalElements, int pageSize) {
        this.content = content;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
    }
 
    // -------------------------------------------------------------------------
    // Content
    // -------------------------------------------------------------------------
 
    public List<T> getContent() {
        return content;
    }
 
    // -------------------------------------------------------------------------
    // Basic page info
    // -------------------------------------------------------------------------
 
    public int getCurrentPage() {
        return currentPage;
    }
 
    public int getTotalPages() {
        return totalPages;
    }
 
    public long getTotalElements() {
        return totalElements;
    }
 
    public int getPageSize() {
        return pageSize;
    }
 
    // -------------------------------------------------------------------------
    // Helpers used directly in Mustache templates
    // -------------------------------------------------------------------------
 
    /** True when there is more than one page (show the pagination bar at all). */
    public boolean isMultiPage() {
        return totalPages > 1;
    }
 
    /** True when this is NOT the first page (show "previous" button). */
    public boolean isHasPrevious() {
        return currentPage > 0;
    }
 
    /** True when this is NOT the last page (show "next" button). */
    public boolean isHasNext() {
        return currentPage < totalPages - 1;
    }
 
    public int getPreviousPage() {
        return currentPage - 1;
    }
 
    public int getNextPage() {
        return currentPage + 1;
    }
 
    /** Human-readable "Page X of Y" numbers (1-based). */
    public int getDisplayPage() {
        return currentPage + 1;
    }
 
    public int getDisplayTotalPages() {
        return totalPages;
    }
 
    /**
     * Builds a list of page-number objects for rendering numbered buttons.
     * Shows up to 5 pages centred around the current page.
     */
    public List<PageNumber> getPageNumbers() {
        List<PageNumber> numbers = new ArrayList<>();
        if (totalPages <= 1) return numbers;
 
        int window = 2; // pages to show on each side of current
        int from = Math.max(0, currentPage - window);
        int to = Math.min(totalPages - 1, currentPage + window);
 
        // Adjust window if near the edges
        if (currentPage - window < 0) {
            to = Math.min(totalPages - 1, to + (window - currentPage));
        }
        if (currentPage + window >= totalPages) {
            from = Math.max(0, from - (window - (totalPages - 1 - currentPage)));
        }
 
        for (int i = from; i <= to; i++) {
            numbers.add(new PageNumber(i, i == currentPage));
        }
        return numbers;
    }
 
    // -------------------------------------------------------------------------
    // Inner class: single page-number button
    // -------------------------------------------------------------------------
 
    public static class PageNumber {
        private final int index;      // 0-based, used in URL ?page=N
        private final boolean active; // is this the current page?
 
        public PageNumber(int index, boolean active) {
            this.index = index;
            this.active = active;
        }
 
        public int getIndex() {
            return index;
        }
 
        /** 1-based label shown on the button. */
        public int getLabel() {
            return index + 1;
        }
 
        public boolean isActive() {
            return active;
        }
    }
}