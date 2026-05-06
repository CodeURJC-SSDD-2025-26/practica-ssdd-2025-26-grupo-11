package es.codeurjc.practica2.restController;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.codeurjc.practica2.dto.DtoMapper;
import es.codeurjc.practica2.dto.UserCreateDTO;
import es.codeurjc.practica2.dto.UserDTO;
import es.codeurjc.practica2.dto.UserUpdateDTO;
import es.codeurjc.practica2.model.PageData;
import es.codeurjc.practica2.model.User;
import es.codeurjc.practica2.service.ReviewService;
import es.codeurjc.practica2.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import es.codeurjc.practica2.model.PageData;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;

    // -------------------------------------------------------
    // GET /api/v1/users
    // Admin: list all users
    // -------------------------------------------------------
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getUsers(
                    @RequestParam(required = false) String q,
                    @RequestParam(defaultValue = "0") int page,
                    @RequestParam(defaultValue = "10") int size,
                    HttpServletRequest request) {

            if (!request.isUserInRole("ADMIN")) {
                    throw new ResponseStatusException(
                                    HttpStatus.FORBIDDEN,
                                    "Solo los administradores pueden listar todos los usuarios.");
            }

            PageData<User> userPageData = userService.searchUsersPage(q, page, size);

            List<UserDTO> dtoList = userPageData.getContent().stream()
                            .map(DtoMapper::toUserDTO)
                            .toList();

            Page<UserDTO> dtoPage = new PageImpl<>(
                            dtoList,
                            PageRequest.of(page, size),
                            userPageData.getTotalElements());

            return ResponseEntity.ok(dtoPage);
    }

    // -------------------------------------------------------
    // POST /api/v1/users
    // Public: register a new user
    // -------------------------------------------------------
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserCreateDTO dto) throws IOException {

        List<String> errors = userService.validateRegistration(
                dto.name(),
                dto.surname(),
                dto.email(),
                dto.password(),
                dto.confirmPassword());

        if (!errors.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.join(" ", errors));
        }

        User user = userService.registerUser(
                dto.name(),
                dto.surname(),
                dto.email(),
                dto.password());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.getId())
                .toUri();

        return ResponseEntity.created(location).body(DtoMapper.toUserDTO(user));
    }

    // -------------------------------------------------------
    // GET /api/v1/users/me
    // User: get own profile
    // -------------------------------------------------------
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile(HttpServletRequest request) {

        User currentUser = getCurrentUser(request);
        return ResponseEntity.ok(DtoMapper.toUserDTO(currentUser));
    }

    // -------------------------------------------------------
    // PUT /api/v1/users/me
    // User: update own profile
    // -------------------------------------------------------
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateMyProfile(
            @RequestBody UserUpdateDTO dto,
            HttpServletRequest request) throws IOException {

        User currentUser = getCurrentUser(request);

        validateUserUpdate(dto, currentUser.getEmail());

        userService.updateProfile(
                currentUser.getId(),
                dto.name(),
                dto.surname(),
                currentUser.getEmail(),
                dto.description(),
                null);

        User updatedUser = userService.findById(currentUser.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se ha encontrado el usuario autenticado."));

        return ResponseEntity.ok(DtoMapper.toUserDTO(updatedUser));
    }

    // -------------------------------------------------------
    // GET /api/v1/users/{id}
    // Admin or owner: get one user profile
    // -------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(
            @PathVariable Long id,
            HttpServletRequest request) {

        User currentUser = getCurrentUser(request);
        User user = findUserOrThrow(id);

        boolean isAdmin = request.isUserInRole("ADMIN");
        boolean isOwner = currentUser.getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para consultar este perfil.");
        }

        return ResponseEntity.ok(DtoMapper.toUserDTO(user));
    }

    // -------------------------------------------------------
    // PUT /api/v1/users/{id}
    // Admin: update any user profile
    // -------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateDTO dto,
            HttpServletRequest request) throws IOException {

        if (!request.isUserInRole("ADMIN")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo los administradores pueden modificar otros usuarios.");
        }

        User user = findUserOrThrow(id);

        validateUserUpdate(dto, user.getEmail());

        userService.updateProfile(
                id,
                dto.name(),
                dto.surname(),
                user.getEmail(),
                dto.description(),
                null);

        User updatedUser = findUserOrThrow(id);
        return ResponseEntity.ok(DtoMapper.toUserDTO(updatedUser));
    }

    // -------------------------------------------------------
    // POST /api/v1/users/{id}/image
    // Admin or owner: upload or replace user image
    // -------------------------------------------------------
    @PostMapping("/{id}/image")
    public ResponseEntity<UserDTO> uploadUserImage(
            @PathVariable Long id,
            @RequestParam MultipartFile imageFile,
            HttpServletRequest request) throws IOException {

        User currentUser = getCurrentUser(request);
        User user = findUserOrThrow(id);

        boolean isAdmin = request.isUserInRole("ADMIN");
        boolean isOwner = currentUser.getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para modificar la imagen de este usuario.");
        }

        validateImageFile(imageFile);

        userService.updateProfile(
                id,
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getDescription(),
                imageFile);

        User updatedUser = findUserOrThrow(id);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/users/{id}/image/media")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).body(DtoMapper.toUserDTO(updatedUser));
    }

    // -------------------------------------------------------
    // GET /api/v1/users/{id}/image/media
    // Public: get user image media
    // -------------------------------------------------------
    @GetMapping("/{id}/image/media")
    public ResponseEntity<Object> getUserImageMedia(@PathVariable Long id) throws SQLException {

        User user = findUserOrThrow(id);

        if (user.getImage() == null || user.getImage().getImageFile() == null) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource imageFile =
                new InputStreamResource(user.getImage().getImageFile().getBinaryStream());

        MediaType mediaType = MediaTypeFactory
                .getMediaType(imageFile)
                .orElse(MediaType.IMAGE_JPEG);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(imageFile);
    }

    // -------------------------------------------------------
    // DELETE /api/v1/users/{id}
    // Admin: delete user
    // -------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            HttpServletRequest request) {

        if (!request.isUserInRole("ADMIN")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo los administradores pueden eliminar usuarios.");
        }

        User userToDelete = findUserOrThrow(id);
        User currentUser = getCurrentUser(request);

        if (userToDelete.getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puedes eliminar tu propia cuenta.");
        }

        userService.deleteUser(id, reviewService);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private User getCurrentUser(HttpServletRequest request) {
        String email = request.getUserPrincipal().getName();

        return userService.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se ha encontrado el usuario autenticado."));
    }

    private User findUserOrThrow(Long id) {
        return userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se ha encontrado el usuario indicado."));
    }

    private void validateUserUpdate(UserUpdateDTO dto, String currentEmail) {
        if (dto.name() == null || dto.name().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre es obligatorio.");
        }

        if (dto.name().length() > 22) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre no puede superar los 22 caracteres.");
        }

        if (dto.surname() == null || dto.surname().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El apellido es obligatorio.");
        }

        if (dto.surname().length() > 22) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El apellido no puede superar los 22 caracteres.");
        }

        if (dto.email() != null && !dto.email().equals(currentEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo electrónico no se puede modificar desde esta operación.");
        }

        if (dto.description() != null && dto.description().length() > 300) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La descripción no puede superar los 300 caracteres.");
        }
    }

    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La imagen no puede estar vacía.");
        }

        String contentType = imageFile.getContentType();

        if (contentType == null
                || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La imagen debe ser .jpg o .png.");
        }
    }
}