# [BiblioOnline]

## 👥 Miembros del Equipo
| Nombre y Apellidos | Correo URJC | Usuario GitHub |
|:--- |:--- |:--- |
| Álvaro Bravo Pareja     | a.bravop.2023@alumnos.urjc.es  | AlvaroBravoPareja      |
| Carlos Asensio Trujillo | c.asensio.2023@alumnos.urjc.es | c-asensio              |
| Ángel Vila Sanchez      | a.vilas.2019@alumnos.urjc.es   | vilasanchezangel-codes |


---

## 🎭 **Preparación: Definición del Proyecto**

### **Theme description**
This project is a digital library management platform within the education and culture sector. The application allows users to explore a comprehensive book catalog, manage loans with automated due dates, and share their reading experiences through a review system.

Value Proposition:

- Centralization: Provides easy and organized access to the library's book inventory.

- Interactivity: Enables readers to actively participate in the community through a rating and commenting system.

- Efficient Management: Ensures rigorous control over loan periods, managing return deadlines and user history.


### **Entities**

1. **[Entity 1]**: [User]
2. **[Entity 2]**: [Book]
3. **[Entity 3]**: [Loan]
4. **[Entity 4]**: [Review]

**Relationships between entities:**
- [User - Loan: A user can have multiple loans over time, but each loan record belongs to a single specific user (1:N)]
- [Book - Loan: A book can be associated with multiple loan records (history), although it is typically linked to one active loan at a time (1:N)]
- [User - Review: A user can write multiple reviews for different books they have read (1:N)]
- [Book - Review: A book can receive multiple reviews and ratings from different users to calculate its average reputation (1:N)]

### **User Permissions**

* **Anonymous User**: 
  - Permissions: [Browse the book catalog, use the search functionality, register for a new account, log in]
  - This user does not own any entities.

* **Registered User**: 
  - Permissions: [Manage their personal profile (including uploading an avatar), request book loans, view their loan history, post reviews for books they have borrowed]
  - Is owner of: [Their own User profile, their Loan records, and their submitted Reviews]

* **Administrator**: 
  - Permissions: [(Create, Read, Update, Delete) operations on the Book catalog, monitoring all Loans, moderating Reviews, and managing User accounts]
  - Is owner of: [All Book entities; has administrative authority over all Loans, Reviews, and Users]

### **Images**

- **[Entity with images 1]**: [User - One image as a profile avatar per user]
- **[Entity with images 2]**: [Book - A representative image of the book cover]

### **Graphics**

- **Graphic 1**: [Most Popular Genres – A pie chart representing the most borrowed genres]
- **Gráfico 2**: [Most Rated Genres – A bar chart displaying the mean of rated reviews for each genre (from 1 to 5 stars)]

### **Complementary Technology**

- [Automatic mail sender using JavaMailSender with information about a loan made by a user]

### **Advanced Algorithm or Query**

- **Algorithm/Query**: [Personalized Book Recommendation System based on User Loan History.]
- **Description**: [The algorithm analyzes the genres of books previously borrowed by the user to identify their reading preferences. It then processes the library catalog to suggest the best rated available titles that match those specific categories]
- **Alternative**: [A query that identifies "Trending Books" by calculating which titles have the highest turnover rate and best ratings within the last 30 days, filtered by the user's favorite genre]

---

## 🛠 **Práctica 1: Maquetación de páginas web con HTML y CSS**

### **Navigation diagram**
Diagram that shows how to navigate between the different pages of the application:

![Navigation diagram](/Practice1/Diagram-images/NavigationDiagram.png)

> Anonimous users can see the most rated books in their home page and also can see the book catalog page.
   The registered users have a different home page where, apart from having a different subheader, they have recomendations based on their previous loans. If a user is registered, the header shows a dropdown menu where you can see your profile, your loans and the option to logout. Finally, administrators can access the admin panel

### **Screenshots and page descriptions**

#### **1. Main Page/Index**
![Index](/Practice1/Diagram-images/index.jpeg)

> Main page of the application, from which unregistered users can access the book catalog, register, or log in. It also displays a selection of featured books (the most rated ones).

#### **2. Register**
![Register](/Practice1/Diagram-images/Registro.jpeg)

> Registration form that allows new users to create an account on the platform.

#### **3. Login**
![Login](/Practice1/Diagram-images/Login.jpeg)

> Login form that allows users to access the platform using their email and password.

#### **4. Books**
![Books](/Practice1/Diagram-images/Books.jpeg)

> Page displaying the catalog of available books, allowing users to browse and filter the library collection.

#### **5. Book Details**
![Book Details](/Practice1/Diagram-images/Book%20details.jpeg)

> Page showing detailed information about a specific book, including the cover, title, author, description, options related to loggued-in users  or admin and viewing/writing reviews.

#### **6. Base**
![Base](/Practice1/Diagram-images/Base.jpeg)

> Base page that acts as the main entry point for authenticated users, providing the general navigation structure after logging in along with book recomendations based on the user previous loans.

#### **7. User Profile**
![UserProfile](/Practice1/Diagram-images/Profile.jpeg)

> User profile page where personal information is displayed and account settings can be managed.

#### **8. Edit User Profile**
![Edit UserProfile](/Practice1/Diagram-images/Edit%20profile.jpeg)

> Page that allows users to edit and update their personal information.

#### **9. My Loans**
![My Loans](/Practice1/Diagram-images/Loans.jpeg)

> Page that displays the user's loans, including active, overdue, or returned books along with their corresponding dates.

#### **10. AdminPanel**
![Admin Panel](/Practice1/Diagram-images/Admin%20panel.jpeg)

> Main administration panel that provides an overview of the system and access to the management of books, loans, reviews, and users. It also shows the graphs.

#### **11. New Book**
![New Book](/Practice1/Diagram-images/New%20book.jpeg)

> Administration form used to add a new book to the library catalog.

#### **12. Edit Book**
![Edit Book](/Practice1/Diagram-images/Edit%20book.jpeg)

> Administration form used to modify the information of an existing book in the system.

#### **13. Edit Loan**
![Edit Loan](/Practice1/Diagram-images/Edit%20loan.jpeg)

> Administration form used to modify the information of an existing loan (Extend loan period).

### **Member Participation in Practice 1**

#### **Student 1 - Alvaro Bravo Pareja**

[I developed the index, base, book search and admin panel pages. I have also fixed other pages I was not originally in charge, mainly css problems, missing buttons, modals and expanding functionalities]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Index page](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/5ca695dca9722519bda616b048475a40a19c10ec)  | [index.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/index.html) / [index.css] (https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/css/index.css)  |
|2| [Base page](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/2f8a91bb7fc0ba6f99e2209003820baba0157a51)  | [base.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/base.html)   |
|3| [Book search page](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/a134a0ecedabf2009ed432e08a2db0990acc1895)  | [books.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/books.html) / [books.css](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/css/books.css)  |
|4| [Admin panel](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/bc9906b43256ade8bbf8b62eaef649147b498bd1)  | [admin-panel.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/admin/admin-panel.html) / [admin-panel.css](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/css/admin-panel.css)   |
|5| [Modals added](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/5fb06ff314d853dd9404824c2befe086242eafe6)  | [book-details.html](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/admin/book-details.html)   |

---

#### **Student 2 - Carlos Asensio Trujillo**

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Added MyLoans page and later updated its style to match the admin panel style and fixed issues](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/d66a099fc125edc550ba40bc36c103664ce44d55)  | [MyLoans/MyLoans css](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/my-loans.css) / [MyLoans](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/my-loans.html)  |
|2| [Added Login page](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/31e38182331066d548df6b167815f80841fb409c)  | [Login](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/login.html)   |
|3| [Added Register page](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/31e38182331066d548df6b167815f80841fb409c)  | [Register](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/register.html)   |
|4| [Added Admin-Edit-book](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/d66a099fc125edc550ba40bc36c103664ce44d55)  | [Admin-Edit-Book](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/admin/admin-edit-book.html)   |
|5| [Added Admin-Edit-loan](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/d66a099fc125edc550ba40bc36c103664ce44d55)  | [Admin-Edit-Loan](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/blob/main/Practice1/admin/admin-edit-loans.html)   |
|6| [Completed README and fixed header issues](https://github.com/CodeURJC-SSDD-2025-26/ssdd-2025-26-project-base/commit/c435bade841fd87879a3a8dcd84a3844bf75b0cd)  | [README](https://github.com/CodeURJC-SSDD-2025-26/practica-ssdd-2025-26-grupo-11/edit/main/README.md)   |

---

#### **Alumno 3 - Ángel Vila Sanchez**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

## 🛠 **Práctica 2: Web con HTML generado en servidor**

### **Navegación y Capturas de Pantalla**

#### **Diagrama de Navegación**

Solo si ha cambiado.

#### **Capturas de Pantalla Actualizadas**

Solo si han cambiado.

### **Instrucciones de Ejecución**

#### **Requisitos Previos**
- **Java**: versión 21 o superior
- **Maven**: versión 3.8 o superior
- **MySQL**: versión 8.0 o superior
- **Git**: para clonar el repositorio

#### **Pasos para ejecutar la aplicación**

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/[usuario]/[nombre-repositorio].git
   cd [nombre-repositorio]
   ```

2. **AQUÍ INDICAR LO SIGUIENTES PASOS**

#### **Credenciales de prueba**
- **Usuario Admin**: usuario: `admin`, contraseña: `admin`
- **Usuario Registrado**: usuario: `user`, contraseña: `user`

### **Diagrama de Entidades de Base de Datos**

Diagrama mostrando las entidades, sus campos y relaciones:

![Diagrama Entidad-Relación](images/database-diagram.png)

> [Descripción opcional: Ej: "El diagrama muestra las 4 entidades principales: Usuario, Producto, Pedido y Categoría, con sus respectivos atributos y relaciones 1:N y N:M."]

### **Diagrama de Clases y Templates**

Diagrama de clases de la aplicación con diferenciación por colores o secciones:

![Diagrama de Clases](images/classes-diagram.png)

> [Descripción opcional del diagrama y relaciones principales]

### **Participación de Miembros en la Práctica 2**

#### **Alumno 1 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 2 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 3 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 4 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

## 🛠 **Práctica 3: API REST, docker y despliegue**

### **Documentación de la API REST**

#### **Especificación OpenAPI**
📄 **[Especificación OpenAPI (YAML)](/api-docs/api-docs.yaml)**

#### **Documentación HTML**
📖 **[Documentación API REST (HTML)](https://raw.githack.com/[usuario]/[repositorio]/main/api-docs/api-docs.html)**

> La documentación de la API REST se encuentra en la carpeta `/api-docs` del repositorio. Se ha generado automáticamente con SpringDoc a partir de las anotaciones en el código Java.

### **Diagrama de Clases y Templates Actualizado**

Diagrama actualizado incluyendo los @RestController y su relación con los @Service compartidos:

![Diagrama de Clases Actualizado](images/complete-classes-diagram.png)

### **Instrucciones de Ejecución con Docker**

#### **Requisitos previos:**
- Docker instalado (versión 20.10 o superior)
- Docker Compose instalado (versión 2.0 o superior)

#### **Pasos para ejecutar con docker-compose:**

1. **Clonar el repositorio** (si no lo has hecho ya):
   ```bash
   git clone https://github.com/[usuario]/[repositorio].git
   cd [repositorio]
   ```

2. **AQUÍ LOS SIGUIENTES PASOS**:

### **Construcción de la Imagen Docker**

#### **Requisitos:**
- Docker instalado en el sistema

#### **Pasos para construir y publicar la imagen:**

1. **Navegar al directorio de Docker**:
   ```bash
   cd docker
   ```

2. **AQUÍ LOS SIGUIENTES PASOS**

### **Despliegue en Máquina Virtual**

#### **Requisitos:**
- Acceso a la máquina virtual (SSH)
- Clave privada para autenticación
- Conexión a la red correspondiente o VPN configurada

#### **Pasos para desplegar:**

1. **Conectar a la máquina virtual**:
   ```bash
   ssh -i [ruta/a/clave.key] [usuario]@[IP-o-dominio-VM]
   ```
   
   Ejemplo:
   ```bash
   ssh -i ssh-keys/app.key vmuser@10.100.139.XXX
   ```

2. **AQUÍ LOS SIGUIENTES PASOS**:

### **URL de la Aplicación Desplegada**

🌐 **URL de acceso**: `https://[nombre-app].etsii.urjc.es:8443`

#### **Credenciales de Usuarios de Ejemplo**

| Rol | Usuario | Contraseña |
|:---|:---|:---|
| Administrador | admin | admin123 |
| Usuario Registrado | user1 | user123 |
| Usuario Registrado | user2 | user123 |

### **OTRA DOCUMENTACIÓN ADICIONAL REQUERIDA EN LA PRÁCTICA**

### **Participación de Miembros en la Práctica 3**

#### **Alumno 1 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 2 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 3 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---

#### **Alumno 4 - [Nombre Completo]**

[Descripción de las tareas y responsabilidades principales del alumno en el proyecto]

| Nº    | Commits      | Files      |
|:------------: |:------------:| :------------:|
|1| [Descripción commit 1](URL_commit_1)  | [Archivo1](URL_archivo_1)   |
|2| [Descripción commit 2](URL_commit_2)  | [Archivo2](URL_archivo_2)   |
|3| [Descripción commit 3](URL_commit_3)  | [Archivo3](URL_archivo_3)   |
|4| [Descripción commit 4](URL_commit_4)  | [Archivo4](URL_archivo_4)   |
|5| [Descripción commit 5](URL_commit_5)  | [Archivo5](URL_archivo_5)   |

---
