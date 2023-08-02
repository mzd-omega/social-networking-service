package kr.co.mz.sns.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_detail", schema = "sns")
public class UserDetailEntity {

  @CreatedBy
  @Id
  @Column(name = "user_seq", nullable = false)
  private Long userSeq;
  @Column(name = "blocked", nullable = false)
  private Boolean blocked;
  @Column(name = "greeting")
  private String greeting;
  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;
  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
  @LastModifiedDate
  @Column(name = "modified_at", nullable = false)
  private LocalDateTime modifiedAt;

  @OneToOne
  @JoinColumn(name = "user_seq")
  private UserEntity user;

  @OneToMany(mappedBy = "userDetailEntity", fetch = FetchType.LAZY)
  private List<FriendRelationshipEntity> friendRelationships;
}
