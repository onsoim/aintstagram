from django.db import models
from django.db.models import IntegerField


class UserModel(models.Model):
    user_id = models.AutoField(primary_key=True)
    name = models.CharField(max_length=100, unique=True, verbose_name="아이디")
    kakaoID = models.IntegerField(unique=True, verbose_name="카카오")
    date = models.DateTimeField(auto_now_add=True, verbose_name="가입날짜")
    profile = models.ImageField(upload_to="pictures/profile", verbose_name="프로필사진")
    text_comment = models.CharField(max_length=30, verbose_name="코멘트")
    post_count = models.IntegerField(verbose_name="포스트", default=0)
    follower_count = models.IntegerField(verbose_name="팔로워", default=0)
    following_count = models.IntegerField(verbose_name="팔로잉", default=0)
    is_open = models.BooleanField(verbose_name="공개 여부", default=True)

    def __str__(self):
        return self.name


class HistoryModel(models.Model):
    history_id = models.AutoField(primary_key=True)
    user = models.ForeignKey(UserModel, verbose_name="사용자", on_delete=models.CASCADE)
    selection = (
        ('F', 'Follow'),
        ('L', 'Like'),
        ('C', 'Comment'),
    )
    type = models.CharField(max_length=1, verbose_name="타입", choices=selection, default='C')
    record_id = models.IntegerField(verbose_name="번호")
    date = models.DateTimeField(auto_now_add=True, verbose_name="생성날짜")
    is_active = models.BooleanField(verbose_name="노출 여부", default=True)
    seen = models.BooleanField(verbose_name="확인 됨", default=False)


class PictureModel(models.Model):
    pic_id = models.AutoField(primary_key=True)
    pic = models.ImageField(upload_to="pictures/pic", verbose_name="사진")
    record_id = models.IntegerField(verbose_name="번호")
    pic_idx = models.IntegerField(verbose_name="인덱스")
    selection = (
        ('P', 'Post'),
        ('S', 'Story'),
        ('M', 'Message')
    )
    type = models.CharField(max_length=1, verbose_name="타입", choices=selection, default='P')


class StoryModel(models.Model):
    story_id = models.AutoField(primary_key=True)
    pic = models.ForeignKey(PictureModel, verbose_name="사진", on_delete=models.CASCADE)
    user = models.ForeignKey(UserModel, verbose_name="유저", on_delete=models.CASCADE)
    date = models.DateTimeField(auto_now_add=True, verbose_name="생성날짜")
    is_active = models.BooleanField(verbose_name="활성화 여부", default=True)


class FollowModel(models.Model):
    follow_id = models.AutoField(primary_key=True)
    user_from = models.ForeignKey(UserModel, verbose_name="요청", related_name="follow_user_from", on_delete=models.CASCADE)
    user_to = models.ForeignKey(UserModel, verbose_name="응답", related_name="follow_user_to", on_delete=models.CASCADE)
    date = models.DateTimeField(auto_now_add=True, verbose_name="생성날짜")


class MessageModel(models.Model):
    message_id = models.AutoField(primary_key=True)
    sender = models.ForeignKey(UserModel, verbose_name="보낸이", on_delete=models.CASCADE)
    chatroom_id = models.IntegerField(verbose_name="번호")
    date = models.DateTimeField(auto_now_add=True, verbose_name="생성날짜")
    has_seen = models.BooleanField(verbose_name="확인여부", default=False)
    pic = models.ForeignKey(PictureModel, blank=True, null=True, verbose_name="사진", on_delete=models.CASCADE)
    text_message = models.CharField(max_length=50, verbose_name="메시지")


class ChatroomModel(models.Model):
    chatroom_id = models.AutoField(primary_key=True)
    user_from = models.ForeignKey(UserModel, verbose_name="생성자", related_name="chatroom_user_from", on_delete=models.CASCADE)
    user_to = models.ForeignKey(UserModel, verbose_name="초대받은자", related_name="chatroom_user_to", on_delete=models.CASCADE)


class HashtagModel(models.Model):
    tag_id = models.AutoField(primary_key=True)
    tag_name = models.CharField(max_length=8, verbose_name="태그")
    selection = (
        ('P', 'Post'),
        ('C', 'Comment'),
    )
    type = models.CharField(max_length=1, verbose_name="타입", choices=selection, default='P')
    record_id = models.IntegerField(verbose_name="번호")


class LikeModel(models.Model):
    like_id = models.AutoField(primary_key=True)
    selection = (
        ('P', 'Post'),
        ('C', 'Comment'),
    )
    type = models.CharField(max_length=1, verbose_name="타입", choices=selection, default='P')
    user_from = models.ForeignKey(UserModel, verbose_name="준 이", related_name="like_user_from", on_delete=models.CASCADE)
    user_to = models.ForeignKey(UserModel, verbose_name="받은 이", related_name="like_user_to", on_delete=models.CASCADE)
    date = models.DateTimeField(auto_now_add=True, verbose_name="생성날짜")
    record_id = models.IntegerField(verbose_name="번호")


class PostModel(models.Model):
    post_id = models.AutoField(primary_key=True)
    user = models.ForeignKey(UserModel, verbose_name="유저", on_delete=models.CASCADE)
    allow_comment = models.BooleanField(default=True, verbose_name="코멘트 활성화여부")
    place = models.CharField(blank=True, max_length=14, verbose_name="장소")
    like_count = models.IntegerField(default=0, verbose_name="좋아요 개수")
    comment_count = models.IntegerField(default=0, verbose_name="댓글 개수")
    text_comment = models.CharField(max_length=100, verbose_name="코멘트")
    date = models.DateTimeField(auto_now_add=True, verbose_name="작성일자")


class CommentModel(models.Model):
    comment_id = models.AutoField(primary_key=True)
    user = models.ForeignKey(UserModel, verbose_name="유저", on_delete=models.CASCADE)
    date = models.DateTimeField(auto_now_add=True, verbose_name="작성일자")
    post_id = models.IntegerField(verbose_name="포스트 번호")
    parent = models.IntegerField(blank=True, null=True)
    like_count = models.IntegerField(default=0, verbose_name="좋아요 개수")
    text_comment = models.CharField(max_length=100, verbose_name="코멘트")
