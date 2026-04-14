// Colors for charts
const chartColors = [
  "#123C4A", // Fiction
  "#5BB7D4", // Mystery
  "#245F73", // Fantasy
  "#28A745", // History
  "#FFC107", // Science
  "#6C757D", // Biography
  "#FA6B5B", // Children
  "#8E44AD", // Romance
  "#E67E22"  // Classics
];

/**
 * Calculates average ratings filtering books with 0 stars
 * @param {Array} books - Array of books with properties: genre, rating
 * @param {Array} genreLabels - Array of genre names
 * @returns {Array} - Array with average ratings per genre
 */
function calculateAverageRatings(books, genreLabels) {
  const ratingByGenre = {};
  
  // Initialize counters
  genreLabels.forEach(label => {
    ratingByGenre[label] = { sum: 0, count: 0 };
  });

  // Group and sum ratings (excluding rating 0)
  books.forEach(book => {
    const genreLabel = book.genre;
    if (ratingByGenre[genreLabel] && book.rating > 0) {
      ratingByGenre[genreLabel].sum += book.rating;
      ratingByGenre[genreLabel].count++;
    }
  });

  // Calculate averages
  return genreLabels.map(label => {
    const data = ratingByGenre[label];
    return data.count > 0 ? (data.sum / data.count).toFixed(2) : 0;
  });
}

/**
 * Extracts book data from the books table DOM
 * @returns {Array} - Array of books with properties: genre, rating
 */
function extractBooksFromDOM() {
  const books = [];
  const genreMap = {
    FICCION: "Ficción",
    FANTASIA: "Fantasía",
    HISTORIA: "Historia",
    CIENCIA: "Ciencia",
    INFANTIL: "Infantil",
    MISTERIO: "Misterio",
    ROMANCE: "Romance",
    BIOGRAFIA: "Biografía",
    CLASICOS: "Clásicos",
  };

  document.querySelectorAll(".book-row").forEach(row => {
    const genre = row.dataset.genre;
    const displayGenre = genreMap[genre] || genre;
    const starsElement = row.querySelector("td:nth-child(6)");
    
    // Count full stars (fas fa-star)
    const filledStars = starsElement ? starsElement.querySelectorAll(".fas.fa-star").length : 0;
    
    books.push({
      genre: displayGenre,
      rating: filledStars
    });
  });

  return books;
}

// Wait for document to be ready
document.addEventListener('DOMContentLoaded', function() {
  // Get data from server or calculate them
  let genreLabels = typeof window.genreLabels !== 'undefined' ? window.genreLabels : [];
  let genreLoanCounts = typeof window.genreLoanCounts !== 'undefined' ? window.genreLoanCounts : [];
  let genreRatingLabels = typeof window.genreRatingLabels !== 'undefined' ? window.genreRatingLabels : [];
  let genreRatingValues = typeof window.genreRatingValues !== 'undefined' ? window.genreRatingValues : [];

  // Try to extract book data from DOM to recalculate ratings (filtering 0 stars)
  try {
    const booksData = extractBooksFromDOM();
    if (booksData.length > 0) {
      genreRatingValues = calculateAverageRatings(booksData, genreRatingLabels);
    }
  } catch (error) {
    console.log('Could not extract book data from DOM, using server values');
  }

  // Pie chart: Most borrowed genres
  const pieCanvas = document.getElementById("genrePieChart");
  if (pieCanvas) {
    new Chart(pieCanvas, {
      type: "doughnut",
      data: {
        labels: genreLabels,
        datasets: [{
          data: genreLoanCounts,
          backgroundColor: chartColors,
          borderColor: "#ffffff",
          borderWidth: 2,
          hoverOffset: 6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: "58%",
        plugins: {
          legend: {
            position: "right",
            labels: {
              usePointStyle: true,
              pointStyle: "circle",
              boxWidth: 10,
              boxHeight: 10,
              padding: 16,
              font: {
                size: 13
              }
            }
          },
          tooltip: {
            callbacks: {
              label: function (context) {
                return `${context.label}: ${context.raw}`;
              }
            }
          }
        }
      }
    });
  }

  // Bar chart: Average rating by genre (excluding books with 0 stars)
  const barCanvas = document.getElementById("ratingBarChart");
  if (barCanvas) {
    new Chart(barCanvas, {
      type: "bar",
      data: {
        labels: genreRatingLabels,
        datasets: [{
          data: genreRatingValues,
          backgroundColor: chartColors,
          borderRadius: 6,
          borderSkipped: false
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            callbacks: {
              label: function (context) {
                return `Valoración: ${context.raw}`;
              }
            }
          }
        },
        scales: {
          x: {
            grid: {
              display: false
            },
            ticks: {
              font: {
                size: 12
              }
            }
          },
          y: {
            beginAtZero: false,
            min: 1,
            max: 5,
            ticks: {
              stepSize: 1,
              font: {
                size: 12
              }
            },
            grid: {
              color: "rgba(0,0,0,0.08)"
            }
          }
        }
      }
    });
  }
});