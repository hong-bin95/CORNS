package com.w6w.corns.service.user;

import com.w6w.corns.domain.loginlog.LoginLogRepository;
import com.w6w.corns.domain.rank.Rank;
import com.w6w.corns.domain.rank.RankRepository;
import com.w6w.corns.domain.user.User;
import com.w6w.corns.domain.user.UserRepository;
import com.w6w.corns.domain.withdraw.Withdraw;
import com.w6w.corns.domain.withdraw.WithdrawLog;
import com.w6w.corns.domain.withdraw.WithdrawLogRepository;
import com.w6w.corns.domain.withdraw.WithdrawRepository;
import com.w6w.corns.dto.explog.ExpLogRequestDto;
import com.w6w.corns.dto.loginlog.LoginLogSaveDto;
import com.w6w.corns.dto.rank.UserRankResponseDto;
import com.w6w.corns.dto.user.*;
import com.w6w.corns.dto.withdraw.WithdrawRequestDto;
import com.w6w.corns.service.growth.GrowthService;
import com.w6w.corns.service.jwt.JwtService;
import com.w6w.corns.util.PageableResponseDto;
import com.w6w.corns.util.SHA256Util;
import com.w6w.corns.util.code.ExpCode;
import com.w6w.corns.util.code.RankCode;
import com.w6w.corns.util.code.UserCode;

import java.io.File;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final WithdrawRepository withdrawRepository;
    private final WithdrawLogRepository withdrawLogRepository;
    private final GrowthService growthService;
    private final JwtService jwtService;
    private final RankRepository rankRepository;

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${domain.save.path}")
    private String domainPath;

    @Override
    @Transactional
    public int signUp(UserJoinRequestDto requestUser) throws Exception {

        //??????????????? ????????? ??????
        int result = validateDuplicateUser(requestUser.getEmail());

        if (result == 1) return -1;
         else {
             //?????????
            String salt = SHA256Util.generateSalt();
            String newPass = SHA256Util.getEncrypt(requestUser.getPassword(), salt);

            userRepository.save(
                    User.userRegister()
                            .email(requestUser.getEmail())
                            .nickname(requestUser.getNickname())
                            .password(newPass) //???????????? ???????????? ??????
                            .salt(salt)
                            .social(1) //?????? ???????????? ???????????? ??????
                            .build()); //?????? ??????
            return 1;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int validateDuplicateUser(String email){
        User findUser = userRepository.findByEmail(email);
        if(findUser == null) return 0; //?????? x
        else return 1; //??????
    }

    @Override
    @Transactional
    public UserDetailResponseDto login(UserLoginRequestDto requestUser) throws Exception{

        //?????? ???????????? ?????? ????????? db?????? ??????
        User user = userRepository.findByEmail(requestUser.getEmail());

        //?????????, ???????????? ?????? ??????
        if(!isSamePassword(requestUser)) return null;

        //?????????????????? ??????????????? 14??? ?????? ??????
        if(user.getUserCd() == UserCode.USER_SUSPEND.getCode() ||
                user.getUserCd() == UserCode.USER_UNREGISTER.getCode() && user.getLastLoginTm().plusDays(14).isBefore(LocalDateTime.now())) return null;

        //?????? ?????? ??????
        user.setUserCd(UserCode.USER_DEFAULT.getCode());
        userRepository.save(user);
        return getUser(user.getUserId());
    }

    @Override
    @Transactional
    public void makeLoginLog(int userId) throws Exception{

        //??????????????? ????????? ??????
        LoginLogSaveDto loginLogSaveDto = LoginLogSaveDto.builder().userId(userId).build();
        loginLogRepository.save(loginLogSaveDto.toEntity());
    }

    @Override
    @Transactional
    public String[] giveToken(int userId) throws Exception {

        //?????? ??????
        String accessToken = jwtService.createAccessToken("id", userId);
        String refreshToken = jwtService.createRefreshToken("id", userId);

        //refresh token ??????
        User user = userRepository.findByUserId(userId);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new String[]{accessToken, refreshToken};
    }

    @Override
    @Transactional(readOnly = true)
    public String getRefreshToken(int userId) throws Exception {
        User user = userRepository.findByUserId(userId);
        return user.getRefreshToken();
    }

    @Override
    @Transactional
    public void deleteRefreshToken(int userId) throws Exception {
        User user = userRepository.findByUserId(userId);
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponseDto getUser(int userId) throws Exception{
        User user = userRepository.findByUserId(userId);

        //?????? ????????? ??????
        UserDetailResponseDto responseDto = UserDetailResponseDto.fromEntity(user);
        List<UserRankResponseDto> userRank = new ArrayList<>();

        for(RankCode rankCd : RankCode.values()){ //null??? ?????? ?????? ??????
            Rank rank = rankRepository.findByUserIdAndRankCd(user.getUserId(), rankCd.getCode());
            if(rank == null){
                userRank.add(UserRankResponseDto.builder()
                        .ranking(-1)
                        .value(0)
                        .rankCd(rankCd.getCode())
                        .build());
            }else{
                userRank.add(UserRankResponseDto.builder()
                        .ranking(rank.getRanking())
                        .value(rank.getValue())
                        .rankCd(rank.getRankCd())
                        .build());
            }
        }
        responseDto.setRank(userRank);
        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponseDto findByEmail(String email) throws Exception{
        User user = userRepository.findByEmail(email);
        return UserDetailResponseDto.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSamePassword(UserLoginRequestDto requestUser) throws Exception {

        User user = userRepository.findByEmail(requestUser.getEmail());

        if(user != null){
            String userSalt = user.getSalt();

            //???????????? user??? password ??????
            String inputPass = requestUser.getPassword();
            String newPass = SHA256Util.getEncrypt(inputPass, userSalt);

            if(newPass.equals(user.getPassword())) return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean updateUserPassword(UserPassModifyRequestDto requestDto) throws Exception{

        User user = userRepository.findByUserId(requestDto.getUserId());

        //???????????? ??????
        if(!isSamePassword(new UserLoginRequestDto(user.getEmail(), requestDto.getPassword()))) return false;

        //???????????? ??????
        String salt = SHA256Util.generateSalt();
        String encryptPass = SHA256Util.getEncrypt(requestDto.getNewPassword(), salt);

        user.setPassword(encryptPass);
        user.setSalt(salt);
        userRepository.save(user);
        return true;
    }

    @Override
    @Transactional
    public UserResponseDto updateUserInfo(UserModifyRequestDto modifyRequestDto, MultipartFile multipartFile) throws Exception{

        User user = userRepository.findByUserId(modifyRequestDto.getUserId());
        //?????? ???????????? ????????? ????????? ????????? null??? ???????????? ?????? ???????????? ??????
        String imgUrl;
        if(multipartFile != null && !multipartFile.isEmpty()){
            String saveUrl = uploadPath + "/users/" + user.getUserId()+"_" + multipartFile.getOriginalFilename();

            //????????? id??? ???????????? ????????? ?????? -> ????????? ?????? ??? ????????? ?????? ?????????
            File dir = new File(uploadPath+"/users/");

            File[] preFiles = dir.listFiles();

            for(File preFile : preFiles){
                StringTokenizer st = new StringTokenizer(preFile.getName(),"_");
                int saveUserId = Integer.parseInt(st.nextToken());

                if(saveUserId == user.getUserId()){
                    //??????
                    log.debug("preFile = " + preFile.getName());
                    preFile.delete();
                    break;
                }
            }

            imgUrl = domainPath + "/users/" + user.getUserId() + "_" + multipartFile.getOriginalFilename();

            File file = new File(saveUrl);

            multipartFile.transferTo(file);

            user.setImgUrl(imgUrl);
        }
        if(modifyRequestDto.getNickname() != null && !modifyRequestDto.getNickname().isEmpty())
            user.setNickname(modifyRequestDto.getNickname());

        userRepository.save(user);

        return UserResponseDto.builder().userId(user.getUserId())
                .nickname(user.getNickname())
                .imgUrl(user.getImgUrl())
                .build();
    }

    @Override
    @Transactional
    public void updateUserCd(int userId, int userCd) throws Exception{

        User user = userRepository.findByUserId(userId);
        user.setUserCd(userCd);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponseDto findAllUserByCondition(Pageable pageable, String baseTime, String filter, String keyword) throws Exception {


        Slice<User> slice = userRepository.findByFilterRegTmLessThanEqual(pageable, baseTime, filter, keyword);

        List<UserListResponseDto> users = new ArrayList<>();
        for(User user : slice.getContent())
            users.add(UserListResponseDto.builder()
                            .userId(user.getUserId())
                            .imgUrl(user.getImgUrl())
                            .nickname(user.getNickname())
                            .levelNo(user.getLevel().getLevelNo())
                            .build());

        return PageableResponseDto.builder()
                .list(users)
                .hasNext(slice.hasNext())
                .build();
    }

    @Override
    @Transactional
    public void withdrawUser(WithdrawRequestDto requestDto) throws Exception {

        //user code ??????
        updateUserCd(requestDto.getUserId(), UserCode.USER_UNREGISTER.getCode());

        //refresh token ??????
        deleteRefreshToken(requestDto.getUserId());

        //???????????? ????????? ??????
        Withdraw withdraw = withdrawRepository.findById(requestDto.getWithdrawNo()).get();

        //????????????
        WithdrawLog withdrawLog = WithdrawLog.builder()
                        .userId(requestDto.getUserId())
                        .withdraw(withdraw)
                        .description(requestDto.getDescription())
                        .build();

        withdrawLogRepository.save(withdrawLog);
    }

    @Override
    @Transactional
    public void checkAttendance(UserDetailResponseDto responseDto) throws Exception {

        User user = userRepository.findByUserId(responseDto.getUserId());

        //?????? ????????? ?????? ??????????????? ?????????
        if (user.getLastLoginTm() == null ||
                !user.getLastLoginTm().toLocalDate().equals(LocalDate.now())) {

            ExpLogRequestDto expLogRequestDto = ExpLogRequestDto.builder()
                    .userId(responseDto.getUserId())
                    .gainExp(3)
                    .expCd(ExpCode.EXP_ATTEND.getCode())
                    .build();
            growthService.giveExp(expLogRequestDto);

            //attendTotal 1??????
            user.setAttendTotal(user.getAttendTotal() + 1);
        }
        user.setLastLoginTm(LocalDateTime.now());
        userRepository.save(user);
    }
}
