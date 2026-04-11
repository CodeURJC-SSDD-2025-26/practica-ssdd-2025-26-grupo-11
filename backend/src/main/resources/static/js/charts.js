// Colores para los gráficos
const chartColors = [
  "#123C4A", // Ficción
  "#5BB7D4", // Misterio
  "#245F73", // Fantasía
  "#28A745", // Historia
  "#FFC107", // Ciencia
  "#6C757D", // Biografía
  "#FA6B5B", // Infantil
  "#8E44AD", // Romance
  "#E67E22"  // Clásicos
];

/**
 * Calcula las valoraciones medias filtrando libros con 0 estrellas
 * @param {Array} books - Array de libros con propiedades: genre, rating
 * @param {Array} genreLabels - Array de nombres de géneros
 * @returns {Array} - Array con las valoraciones medias por género
 */
function calculateAverageRatings(books, genreLabels) {
  const ratingByGenre = {};
  
  // Inicializar contadores
  genreLabels.forEach(label => {
    ratingByGenre[label] = { sum: 0, count: 0 };
  });

  // Agrupar y sumar valoraciones (excluyendo rating 0)
  books.forEach(book => {
    const genreLabel = book.genre;
    if (ratingByGenre[genreLabel] && book.rating > 0) {
      ratingByGenre[genreLabel].sum += book.rating;
      ratingByGenre[genreLabel].count++;
    }
  });

  // Calcular promedios
  return genreLabels.map(label => {
    const data = ratingByGenre[label];
    return data.count > 0 ? (data.sum / data.count).toFixed(2) : 0;
  });
}

/**
 * Extrae datos de libros del DOM de la tabla de libros
 * @returns {Array} - Array de libros con propiedades: genre, rating
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
    
    // Contar estrellas llenas (fas fa-star)
    const filledStars = starsElement ? starsElement.querySelectorAll(".fas.fa-star").length : 0;
    
    books.push({
      genre: displayGenre,
      rating: filledStars
    });
  });

  return books;
}

// Esperar a que el documento esté listo
document.addEventListener('DOMContentLoaded', function() {
  // Obtener datos del servidor o calcularlos
  let genreLabels = typeof window.genreLabels !== 'undefined' ? window.genreLabels : [];
  let genreLoanCounts = typeof window.genreLoanCounts !== 'undefined' ? window.genreLoanCounts : [];
  let genreRatingLabels = typeof window.genreRatingLabels !== 'undefined' ? window.genreRatingLabels : [];
  let genreRatingValues = typeof window.genreRatingValues !== 'undefined' ? window.genreRatingValues : [];

  // Intentar extraer datos de libros del DOM para recalcular valoraciones (filtrando 0 estrellas)
  try {
    const booksData = extractBooksFromDOM();
    if (booksData.length > 0) {
      genreRatingValues = calculateAverageRatings(booksData, genreRatingLabels);
    }
  } catch (error) {
    console.log('No se pudieron extraer datos de libros del DOM, usando valores del servidor');
  }

  // Gráfico de Pastel: Géneros más prestados
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

  // Gráfico de Barras: Valoración media por género (excluyendo libros con 0 estrellas)
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