package pe.edu.upeu.pollogordo_api.services;

import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {

    void init();

    String store(MultipartFile file);

    void delete(String filename);
}