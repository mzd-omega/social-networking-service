package kr.co.mz.sns.service.user;

import java.util.List;
import kr.co.mz.sns.dto.comment.NotificationDto;
import kr.co.mz.sns.dto.user.detail.CompleteUserDetailDto;
import kr.co.mz.sns.dto.user.detail.InsertUserDetailDto;
import kr.co.mz.sns.dto.user.detail.UpdateUserDetailDto;
import kr.co.mz.sns.dto.user.detail.UserDetailAndProfileDto;
import kr.co.mz.sns.dto.user.friend.AFriendDto;
import kr.co.mz.sns.entity.comment.CommentNotificationEntity;
import kr.co.mz.sns.entity.user.UserDetailEntity;
import kr.co.mz.sns.exception.NotFoundException;
import kr.co.mz.sns.repository.comment.CommentNotificationRepository;
import kr.co.mz.sns.repository.user.UserDetailRepository;
import kr.co.mz.sns.util.CurrentUserInfo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class UserDetailService {

  private final UserDetailRepository userDetailRepository;
  private final UserProfileService userProfileService;
  private final UserService userService;
  private final ModelMapper modelMapper;
  private final CommentNotificationRepository commentNotificationRepository;
  private final CurrentUserInfo currentUserInfo;

  public CompleteUserDetailDto findByUserSeq(Long userSeq) {
    return modelMapper
        .map(
            userDetailRepository
                .findById(userSeq)
                .orElseGet(UserDetailEntity::new),
            CompleteUserDetailDto.class
        );
  }

  public CompleteUserDetailDto findByUserName(String userName) {
    return modelMapper
        .map(
            userDetailRepository
                .findByName(userName)
                .orElseGet(UserDetailEntity::new),
            CompleteUserDetailDto.class
        );
  }

  public
  public UserDetailAndProfileDto findByEmail(String email) {
    var userSeq = userService.findByUserEmail(email).getSeq();

    var findUserDetailDto = modelMapper
        .map(
            userDetailRepository
                .findById(userSeq)
                .orElseThrow(
                    () -> new NotFoundException("등록된 상세 정보가 없습니다. 지금 바로 작성 해 보세 요!")
                ),
            UserDetailAndProfileDto.class);
    findUserDetailDto.setUserDetailFileDtoSet(userProfileService.findAll(userSeq));

    return findUserDetailDto;
  }

  @Transactional
  public CompleteUserDetailDto insert(InsertUserDetailDto insertUserDetailDto) {
    var userDetailEntity = modelMapper.map(insertUserDetailDto, UserDetailEntity.class);
    //todo insertfailed exception 이녀석은 바로위의 엔티티랑 같은녀석이다 참조까지 같다. jpa설명에있다.
    return modelMapper
        .map(
            userDetailRepository.save(userDetailEntity),
            CompleteUserDetailDto.class);
  }

  @Transactional
  public CompleteUserDetailDto updateByUserSeq(UpdateUserDetailDto updateUserDetailDto) {
    var optionalUserDetailEntity = userDetailRepository.findByUserSeq(updateUserDetailDto.getUserSeq());
    var userDetailEntity = optionalUserDetailEntity.orElseThrow(
        () -> new NotFoundException("Oops! No existing detail! Insert your detail first!"));
    userDetailEntity.setGreeting(updateUserDetailDto.getGreeting());
    //todo fileEntity가 필요하네..?
//    var userDetailEntity = modelMapper.map(updateUserDetailDto, UserDetailEntity.class);
    var updatedEntity = userDetailRepository.save(userDetailEntity);

    return modelMapper
        .map(updatedEntity, CompleteUserDetailDto.class);
  }

  //나는 삭제만 할거라고 알고 있다. 근데 다른 사람이 이녀석을 쓸 때, 트랜잭션 걸고할껏.
  //일단 트랜잭션공부가 더 필요.
  @Transactional
  public CompleteUserDetailDto deleteByUserSeq(Long userSeq) {
    return modelMapper
        .map(
            userDetailRepository.deleteByUserSeq(userSeq), CompleteUserDetailDto.class
        );
  }

  // 완벽하게 dto 랑 맞지 않으니까.왜? default 로 생성되는 db설정들 외 기타 요인들 떄문.
  // 업데이트 후 업데이트 된 entity 를 다시 dto로 만들어서 보내준다.

  @Transactional
  public List<NotificationDto> mention(List<String> mentionedNames) {
    return mentionedNames.stream()
        .map(
            name -> findByUserName(name).getUserSeq()
        )
        .map(userSeq -> {
          var notiEntity = new CommentNotificationEntity();
          notiEntity.setMentionerSeq(currentUserInfo.getSeq());
          notiEntity.setReadStatus(false);
          notiEntity.setTargetSeq(userSeq);
          return notiEntity;
        })
        .map(commentNotificationRepository::save)
        .toList()
        .stream().map(notiEntity -> modelMapper.map(notiEntity,
            NotificationDto.class))
        .toList();
  }

  public AFriendDto findByFriendName(String friendName) {
    var user = userDetailRepository.findByName(friendName);
    return null;
  }
}
