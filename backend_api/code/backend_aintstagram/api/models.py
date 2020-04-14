from django.db import models


class UserModel(models.Model):
    name = models.CharField(max_length=100, unique=True, verbose_name="아이디")
    kakaoID = models.IntegerField(unique=True, verbose_name="카카오")
    date = models.DateTimeField(auto_now_add=True, verbose_name="가입날짜")

    def __str__(self):
        return self.name

