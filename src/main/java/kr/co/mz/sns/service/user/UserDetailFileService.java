package kr.co.mz.sns.service.user;

import static kr.co.mz.sns.file.FileConstants.SALVE_LOCAL_DERICTORY;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import kr.co.mz.sns.dto.user.GenericUserDetailFileDto;
import kr.co.mz.sns.entity.user.UserDetailFileEntity;
import kr.co.mz.sns.exception.FileWriteException;
import kr.co.mz.sns.file.FileStorageService;
import kr.co.mz.sns.repository.user.UserDetailFileRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class UserDetailFileService {

  private final UserDetailFileRepository userDetailFileRepository;
  private final ModelMapper modelMapper;

  public void saveFile(List<MultipartFile> fileList, String uuid) {
    for (var file : fileList) {
      if (!file.isEmpty()) {
        var directory = createDirectory();
        var targetFile = directory + File.separator + uuid;
        try (
            var bis = new BufferedInputStream(file.getInputStream());
            var bos = new BufferedOutputStream(new FileOutputStream(targetFile))
        ) {
          var buffer = new byte[4096];
          int bytesRead;//한번에읽은바이트 수
          while ((bytesRead = bis.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);// 버퍼의, 0(처음)부터, 읽은만큼
          }
        } catch (IOException ioe) {
          throw new FileWriteException("Failed to save file", ioe);
        }
      }
    }
  }

  public String createDirectory() {
    var fileDirectory = new File(SALVE_LOCAL_DERICTORY + LocalDateTime.now().toLocalDate().toString());
    if (!fileDirectory.mkdirs()) {
      System.out.println("경로가 존재합니다.");
    }
    return fileDirectory.getAbsolutePath();
  }

  public GenericUserDetailFileDto findByName(GenericUserDetailFileDto fileDto) {
    return modelMapper.map(userDetailFileRepository.findByName(fileDto.getName()), GenericUserDetailFileDto.class);
  }

  @Transactional
  public List<GenericUserDetailFileDto> insertProfiles(List<MultipartFile> files) {
    return FileStorageService.getUserFileList(files).stream()
        .map(dto -> userDetailFileRepository.save(modelMapper.map(dto, UserDetailFileEntity.class)))
        .map(entity -> modelMapper.map(entity, GenericUserDetailFileDto.class))
        .toList();
  }
}
