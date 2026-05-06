package es.codeurjc.practica2.restController;

import es.codeurjc.practica2.model.Genre;
import es.codeurjc.practica2.repository.LoanRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import es.codeurjc.practica2.service.BookService;

@RestController
@RequestMapping("/api/v1")
public class GraphRestController {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookService bookService;

    @GetMapping("/charts")
    public ResponseEntity<Map<String, Object>> getChartData() {

        Map<Genre, Long> loanCountMap = new HashMap<>();
        for (Object[] row : loanRepository.countLoansByGenre()) {
            loanCountMap.put((Genre) row[0], (Long) row[1]);
        }

        Map<Genre, Double> ratingMap = new HashMap<>();
        for (Object[] row : bookService.avgRatingByGenre()) {
            ratingMap.put((Genre) row[0], (Double) row[1]);
        }

        List<String> genreLabels = new ArrayList<>();
        List<Long> genreLoanCounts = new ArrayList<>();
        List<Double> genreRatingValues = new ArrayList<>();

        for (Genre genre : Genre.values()) {
            genreLabels.add(genre.getDisplayName());
            genreLoanCounts.add(loanCountMap.getOrDefault(genre, 0L));
            double avg = ratingMap.getOrDefault(genre, 0.0);
            genreRatingValues.add(Math.round(avg * 10.0) / 10.0);
        }

        return ResponseEntity.ok(Map.of(
                "genreLabels", genreLabels,
                "genreLoanCounts", genreLoanCounts,
                "genreRatingValues", genreRatingValues));
    }
}