package kr.co.mz.sns.service.post;

import java.util.List;
import kr.co.mz.sns.dto.post.GenericPostFileDto;
import kr.co.mz.sns.dto.post.PostLikeDto;
import kr.co.mz.sns.dto.post.PostSearchDto;
import kr.co.mz.sns.dto.post.SelectPostDto;
import kr.co.mz.sns.entity.post.PostEntity;
import kr.co.mz.sns.exception.NotFoundException;
import kr.co.mz.sns.file.FileStorageService;
import kr.co.mz.sns.repository.post.PostRepository;
import kr.co.mz.sns.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeService postLikeService;
    private final PostFileService postFileService;
    private final CommentService commentService;
    private final ModelMapper modelMapper;

    public List<SelectPostDto> findByKeyword(PostSearchDto postSearchDto, Pageable pageable) {
        return postRepository.findByContentContaining(postSearchDto.getKeyword(), pageable).stream()
            .map(post -> modelMapper.map(post, SelectPostDto.class))
            .toList();
    }

    public List<SelectPostDto> findAll(Pageable pageable) {
        return postRepository.findAll(pageable).map(post -> modelMapper.map(post, SelectPostDto.class)).toList();
        // 구현 안된 메서드에 보통 아래의 익셉션을 날림
//        throw new UnsupportedOperationException();
    }

    public SelectPostDto findByKey(Long seq) {
        // Declarative Programming or Functional Programming
        return postRepository.findById(seq)
            .map(entity -> modelMapper.map(entity, SelectPostDto.class))
            .orElseThrow(() -> new NotFoundException("This post does not exist"));

        // Imperative Programming
//        var optionalPost = postRepository.findById(seq);
//        optionalPost.orElseThrow(() -> new NotFoundException("This post does not exist"));
//        return modelMapper.map(optionalPost.get(), PostDto.class);
    }

    @Transactional
    public List<GenericPostFileDto> insert(SelectPostDto selectPostDto, List<MultipartFile> files) {
        var postEntity = postRepository.save(modelMapper.map(selectPostDto, PostEntity.class));
        return postFileService.insert(FileStorageService.getPostFileList(files)
            , modelMapper.map(postEntity, SelectPostDto.class));
    }

    @Transactional
    public SelectPostDto updateBySeq(Long seq, SelectPostDto selectPostDto) {
        // Declarative Programming
        return postRepository.findById(seq)
            .map(entity -> {
                entity.setContent(selectPostDto.getContent());
                return entity;
            })
            .map(postRepository::save)
            .map(entity -> modelMapper.map(entity, SelectPostDto.class))
            .orElseThrow(() -> new NotFoundException("Post with ID " + seq + "not found"));

        // Imperative Programming
//        var optionalPost = postRepository.findById(seq);
//        var postEntity = optionalPost.orElseThrow(
//            () -> new NotFoundException("Post with ID " + seq + "not found", 1));
//        var userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        var optionalUserEntity = userRepository.findByEmail(userDetails.getUsername());
//        postEntity.setUsers(
//            optionalUserEntity.orElseThrow(() -> new NotFoundException("User information not found", 1))
//        );
//        postEntity.setContent(postDto.getContent());
//        return modelMapper.map(postRepository.save(postEntity), PostDto.class);
    }

    @Transactional
    public void deleteBySeq(Long seq) {
        // Declarative 1
        postRepository.findById(seq)
            .map(entity -> {
                postRepository.delete(entity);
                return entity;
            })
            .orElseThrow(() -> new NotFoundException("Post with ID " + seq + "not found"));
        // Declarative 2
//        postRepository.findById(seq)
//            .ifPresentOrElse(
//                postRepository::delete,
//                () -> {
//                    throw new NotFoundException("Post with ID " + seq + "not found");
//                }
//            );
//
//        // Imperative
//        var optionalPostEntity = postRepository.findById(seq);
//        var postEntity = optionalPostEntity.orElseThrow(
//            () -> new NotFoundException("Post with ID " + seq + "not found"));
//        postRepository.delete(postEntity);
    }

    @Transactional
    public PostLikeDto like(Long seq) {
        postRepository.findById(seq)
            .map((entity) -> {
                entity.setLikes(entity.getLikes() + 1);
                return postRepository.save(entity);
            });

        return postLikeService.insert(seq);
    }
}