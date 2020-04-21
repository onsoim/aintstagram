from django.contrib import admin
from .models import *

admin.site.register(UserModel)
admin.site.register(HistoryModel)
admin.site.register(StoryModel)
admin.site.register(PictureModel)
admin.site.register(PostModel)
admin.site.register(HashtagModel)
admin.site.register(LikeModel)
admin.site.register(CommentModel)
admin.site.register(FollowModel)
admin.site.register(MessageModel)
admin.site.register(ChatroomModel)