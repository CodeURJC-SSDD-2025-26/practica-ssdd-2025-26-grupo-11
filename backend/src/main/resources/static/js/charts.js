// Graph 1: Doughnut
const pieCtx = document.getElementById('genrePieChart').getContext('2d');
new Chart(pieCtx, {
    type: 'doughnut',
    data: {
        labels: ['Ficción', 'Misterio', 'Fantasía', 'Historia', 'Ciencia', 'Biografía', 'Infantil', 'Romance', 'Clásicos'],
        datasets: [{
            data: [35, 20, 18, 13, 8, 6, 7, 5, 4],
            backgroundColor: ['#113744', '#5bc8e8', '#1a5568', '#28a745', '#ffc107', '#6c757d', '#ff6f61', '#8e44ad', '#e67e22'],
            borderWidth: 3,
            borderColor: '#fff',
            hoverOffset: 6
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'right',
                labels: { font: { size: 12 }, padding: 12, usePointStyle: true }
            },
            tooltip: {
                callbacks: { label: ctx => ` ${ctx.label}: ${ctx.parsed}%` }
            }
        },
        cutout: '58%'
    }
});

// Graph 2: Bar
const barCtx = document.getElementById('ratingBarChart').getContext('2d');
new Chart(barCtx, {
    type: 'bar',
    data: {
        labels: ['Ficción', 'Misterio', 'Fantasía', 'Historia', 'Ciencia', 'Biografía', 'Infantil', 'Romance', 'Clásicos'],
        datasets: [{
            label: 'Valoración media',
            data: [4.6, 4.4, 4.7, 4.2, 3.9, 4.1, 4.3, 4.0, 4.5],
            backgroundColor: ['#113744', '#5bc8e8', '#1a5568', '#28a745', '#ffc107', '#6c757d', '#ff6f61', '#8e44ad', '#e67e22'],
            borderRadius: 6,
            borderSkipped: false
        }]
    },
    options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
            y: {
                beginAtZero: false,
                min: 1, max: 5,
                ticks: { stepSize: 1, font: { size: 11 } },
                grid: { color: '#e9ecef' }
            },
            x: {
                grid: { display: false },
                ticks: { font: { size: 11 } }
            }
        },
        plugins: {
            legend: { display: false },
            tooltip: {
                callbacks: { label: ctx => ` ${ctx.parsed.y} ★` }
            }
        }
    }
});