# Generated by Django 3.0.3 on 2020-05-19 14:35

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    initial = True

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='HashtagModel',
            fields=[
                ('tag_id', models.AutoField(primary_key=True, serialize=False)),
                ('tag_name', models.CharField(max_length=8, verbose_name='태그')),
                ('type', models.CharField(choices=[('P', 'Post'), ('C', 'Comment')], default='P', max_length=1, verbose_name='타입')),
                ('record_id', models.IntegerField(verbose_name='번호')),
            ],
        ),
        migrations.CreateModel(
            name='PictureModel',
            fields=[
                ('pic_id', models.AutoField(primary_key=True, serialize=False)),
                ('pic', models.ImageField(upload_to='pictures/pic', verbose_name='사진')),
                ('record_id', models.IntegerField(verbose_name='번호')),
                ('pic_idx', models.IntegerField(verbose_name='인덱스')),
                ('type', models.CharField(choices=[('P', 'Post'), ('S', 'Story'), ('M', 'Message')], default='P', max_length=1, verbose_name='타입')),
            ],
        ),
        migrations.CreateModel(
            name='UserModel',
            fields=[
                ('user_id', models.AutoField(primary_key=True, serialize=False)),
                ('name', models.CharField(max_length=100, unique=True, verbose_name='아이디')),
                ('kakaoID', models.IntegerField(unique=True, verbose_name='카카오')),
                ('date', models.DateTimeField(auto_now_add=True, verbose_name='가입날짜')),
                ('profile', models.ImageField(upload_to='pictures/profile', verbose_name='프로필사진')),
                ('text_comment', models.CharField(max_length=30, verbose_name='코멘트')),
                ('post_count', models.IntegerField(default=0, verbose_name='포스트')),
                ('follower_count', models.IntegerField(default=0, verbose_name='팔로워')),
                ('following_count', models.IntegerField(default=0, verbose_name='팔로잉')),
                ('is_open', models.BooleanField(default=True, verbose_name='공개 여부')),
            ],
        ),
        migrations.CreateModel(
            name='StoryModel',
            fields=[
                ('story_id', models.AutoField(primary_key=True, serialize=False)),
                ('date', models.DateTimeField(auto_now_add=True, verbose_name='생성날짜')),
                ('is_active', models.BooleanField(default=True, verbose_name='활성화 여부')),
                ('pic', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='api.PictureModel', verbose_name='사진')),
                ('user', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='api.UserModel', verbose_name='유저')),
            ],
        ),
        migrations.CreateModel(
            name='PostModel',
            fields=[
                ('post_id', models.AutoField(primary_key=True, serialize=False)),
                ('allow_comment', models.BooleanField(default=True, verbose_name='코멘트 활성화여부')),
                ('place', models.CharField(max_length=14, verbose_name='장소')),
                ('like_count', models.IntegerField(default=0, verbose_name='좋아요 개수')),
                ('comment_count', models.IntegerField(blank=True, default=0, null=True)),
                ('text_comment', models.CharField(max_length=100, verbose_name='코멘트')),
                ('date', models.DateTimeField(auto_now_add=True, verbose_name='작성일자')),
                ('user', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='api.UserModel', verbose_name='유저')),
            ],
        ),
        migrations.CreateModel(
            name='MessageModel',
            fields=[
                ('message_id', models.AutoField(primary_key=True, serialize=False)),
                ('chatroom_id', models.IntegerField(verbose_name='번호')),
                ('date', models.DateTimeField(auto_now_add=True, verbose_name='생성날짜')),
                ('has_seen', models.BooleanField(default=False, verbose_name='확인여부')),
                ('text_message', models.CharField(max_length=50, verbose_name='메시지')),
                ('pic', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='api.PictureModel', verbose_name='사진')),
                ('sender', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='api.UserModel', verbose_name='보낸이')),
            ],
        ),
        migrations.CreateModel(
            name='LikeModel',
            fields=[
                ('like_id', models.AutoField(primary_key=True, serialize=False)),
                ('type', models.CharField(choices=[('P', 'Post'), ('C', 'Comment')], default='P', max_length=1, verbose_name='타입')),
                ('date', models.DateTimeField(auto_now_add=True, verbose_name='생성날짜')),
                ('record_id', models.IntegerField(verbose_name='번호')),
                ('user_from', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='like_user_from', to='api.UserModel', verbose_name='준 이')),
                ('user_to', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='like_user_to', to='api.UserModel', verbose_name='받은 이')),
            ],
        ),
        migrations.CreateModel(
            name='HistoryModel',
            fields=[
                ('history_id', models.AutoField(primary_key=True, serialize=False)),
                ('type', models.CharField(choices=[('F', 'Follow'), ('L', 'Like'), ('C', 'Comment')], default='C', max_length=1, verbose_name='타입')),
                ('record_id', models.IntegerField(verbose_name='번호')),
                ('date', models.DateTimeField(auto_now_add=True, verbose_name='생성날짜')),
                ('is_active', models.BooleanField(default=True, verbose_name='노출 여부')),
                ('user', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='api.UserModel', verbose_name='사용자')),
            ],
        ),
        migrations.CreateModel(
            name='FollowModel',
            fields=[
                ('follow_id', models.AutoField(primary_key=True, serialize=False)),
                ('date', models.DateTimeField(auto_now_add=True, verbose_name='생성날짜')),
                ('user_from', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='follow_user_from', to='api.UserModel', verbose_name='요청')),
                ('user_to', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='follow_user_to', to='api.UserModel', verbose_name='응답')),
            ],
        ),
        migrations.CreateModel(
            name='CommentModel',
            fields=[
                ('comment_id', models.AutoField(primary_key=True, serialize=False)),
                ('date', models.DateTimeField(auto_now_add=True, verbose_name='작성일자')),
                ('post_id', models.IntegerField(verbose_name='포스트 번호')),
                ('parent', models.IntegerField(null=True)),
                ('like_count', models.IntegerField(default=0, verbose_name='좋아요 개수')),
                ('text_comment', models.CharField(max_length=100, verbose_name='코멘트')),
                ('user', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='api.UserModel', verbose_name='유저')),
            ],
        ),
        migrations.CreateModel(
            name='ChatroomModel',
            fields=[
                ('chatroom_id', models.AutoField(primary_key=True, serialize=False)),
                ('user_from', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='chatroom_user_from', to='api.UserModel', verbose_name='생성자')),
                ('user_to', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='chatroom_user_to', to='api.UserModel', verbose_name='초대받은자')),
            ],
        ),
    ]
