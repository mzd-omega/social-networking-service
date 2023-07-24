package kr.co.mz.sns.service;

import kr.co.mz.sns.dto.UserDetailDto;
import kr.co.mz.sns.entity.UserDetailEntity;
import kr.co.mz.sns.repository.UserDetailRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

@Repository
public class UserDetailService {

  private final UserDetailRepository userDetailRepository;
  private final ModelMapper modelMapper;

  @Autowired
  public UserDetailService(UserDetailRepository userDetailRepository, ModelMapper modelMapper) {
    this.userDetailRepository = userDetailRepository;
    this.modelMapper = modelMapper;
  }

  public UserDetailDto findById(Long userSeq) {
    if (!userDetailRepository.existsByUserSeq(userSeq)) {
      throw new EmptyResultDataAccessException("Expected 1 result, but none returned.", 1);
    }
    return modelMapper.map(userDetailRepository.findById(userSeq).orElseGet(UserDetailEntity::new),
        UserDetailDto.class);
  }

  public UserDetailDto saveOne(UserDetailDto userDetailDto) {
    var userDetailEntity = modelMapper.map(userDetailDto, UserDetailEntity.class);
    var savedEntity = userDetailRepository.save(userDetailEntity);
    return modelMapper.map(savedEntity, UserDetailDto.class);
  }

  public UserDetailDto updateByUserSeq(UserDetailDto userDetailDto) {
    if (!userDetailRepository.existsByUserSeq(userDetailDto.getUser_seq())) {
      throw new EmptyResultDataAccessException("Expected 1 result, but none returned.", 1);
    }
    var userDetailEntity = modelMapper.map(userDetailDto, UserDetailEntity.class);
    var updatedEntity = userDetailRepository.save(userDetailEntity);
    return modelMapper.map(updatedEntity, UserDetailDto.class);
  }

  public void deleteById(Long userSeq) {
    userDetailRepository.deleteById(userSeq);//empty result data exception 발생.
  }

  public UserDetailDto updateUserDetail(UserDetailDto userDetailDto) {
    var userDetailEntity = modelMapper.map(userDetailDto, UserDetailEntity.class);
    var updatedUserDetailEntity = userDetailRepository.save(userDetailEntity);
    // 완벽하게 dto 랑 맞지 않으니까.왜? default 로 생성되는 db설정들 외 기타 요인들 떄문.
    // 업데이트 후 업데이트 된 entitiy 를 다시 dto로 만들어서 보내준다.
    return modelMapper.map(updatedUserDetailEntity, UserDetailDto.class);
  }

  public UserDetailDto toDto(UserDetailEntity userDetailEntity) {
    return modelMapper.map(userDetailEntity, UserDetailDto.class);
  }

}
