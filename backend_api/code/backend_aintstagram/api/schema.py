import graphene
from graphene_django import DjangoObjectType
from graphene_file_upload.scalars import Upload
from api.models import *
from api.token import *
from graphene import relay, Node
from graphql import GraphQLError


class UserType(DjangoObjectType):
    class Meta:
        model = UserModel


class PostType(DjangoObjectType):
    class Meta:
        model = PostModel


class PictureType(DjangoObjectType):
    class Meta:
        model = PictureModel


class FollowType(DjangoObjectType):
    class Meta:
        model = FollowModel


class LikeType(DjangoObjectType):
    class Meta:
        model = LikeModel


class CommentType(DjangoObjectType):
    class Meta:
        model = CommentModel


class HistoryType(DjangoObjectType):
    class Meta:
        model = HistoryModel


class ChatroomType(DjangoObjectType):
    class Meta:
        model = ChatroomModel


class MessageType(DjangoObjectType):
    class Meta:
        model = MessageModel


class Query(graphene.ObjectType):
    users = graphene.List(UserType,
                          kakaoID=graphene.Int(),
                          username=graphene.String(),
                          accessToken=graphene.String(),
                          search=graphene.Int(),
                          )

    posts = graphene.List(PostType,
                          username=graphene.String(),
                          accessToken=graphene.String(required=True),
                          record=graphene.Int(),
                          )

    pics = graphene.List(PictureType,
                         record=graphene.Int(),
                         username=graphene.String(),
                         accessToken=graphene.String(required=True),
                         )

    follows = graphene.List(FollowType,
                            accessToken=graphene.String(required=True),
                            username=graphene.String(),
                            choice=graphene.Int(),
                            )

    likes = graphene.List(LikeType,
                          accessToken=graphene.String(required=True),
                          typeinfo=graphene.String(required=True),
                          record=graphene.Int(required=True),
                          username=graphene.String(),
                          )

    comments = graphene.List(CommentType,
                             accessToken=graphene.String(required=True),
                             record=graphene.Int(required=True),
                             parent=graphene.Int(),
                             )

    histories = graphene.List(HistoryType,
                              accessToken=graphene.String(required=True),
                              )

    chatrooms = graphene.List(ChatroomType,
                              accessToken=graphene.String(required=True),
                              username=graphene.String(),
                              )

    messages = graphene.List(MessageType,
                             accessToken=graphene.String(required=True),
                             username=graphene.String(required=True),
                             )

    def resolve_users(self, info, kakaoID=None, username=None, accessToken=None, search=None):
        query = UserModel.objects.all()

        if kakaoID:
            return UserModel.objects.filter(kakaoID=kakaoID)

        if search == 1:
            return UserModel.objects.filter(name__icontains=username)

        elif username:
            return UserModel.objects.filter(name=username)

        elif accessToken:
            kakaoID = get_kakaoID(accessToken)
            if kakaoID is not None:
                return UserModel.objects.filter(kakaoID=kakaoID)
        else:
            return query

    def resolve_posts(self, info, username=None, accessToken=None, record=None):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            raise GraphQLError("Not Permitted")

        if record:
            posts = PostModel.objects.filter(user__kakaoID=kakaoID, post_id=record)
            return posts

        if username:
            posts = PostModel.objects.filter(user__name=username).order_by('date').reverse()
            return posts

        else:
            posts = PostModel.objects.filter(user__name=UserModel.objects.get(kakaoID=kakaoID))
            for following in list(FollowModel.objects.filter(user_from__kakaoID=kakaoID).values("user_to_id")):
                posts |= PostModel.objects.filter(user__name=UserModel.objects.get(user_id=following["user_to_id"]))
            return posts.order_by('date').reverse()

    def resolve_pics(self, info, username=None, record=None, accessToken=None):
        if record:
            return PictureModel.objects.filter(record_id=record)

        elif username:
            return PictureModel.objects.filter(
                record_id__in=PostModel.objects.filter(user__name=username).values('post_id')).order_by(
                'pic_id').reverse()

        else:
            kakaoID = get_kakaoID(accessToken)
            if kakaoID is None:
                raise GraphQLError("Not Permitted")

            return PictureModel.objects.filter(
                record_id__in=PostModel.objects.filter(user__kakaoID=kakaoID).values('post_id')).order_by(
                'pic_id').reverse()

    def resolve_follows(self, info, accessToken, username=None, choice=None):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            raise GraphQLError("Not Permitted")

        if username is not None:
            follow = FollowModel.objects.filter(user_from__kakaoID=kakaoID, user_to__name=username)
            return follow

        else:
            if choice == 1:
                return FollowModel.objects.filter(user_to__kakaoID=kakaoID)
            elif choice == 2:
                return FollowModel.objects.filter(user_from__kakaoID=kakaoID)
            else:
                raise GraphQLError("Error")

    def resolve_likes(self, info, accessToken, typeinfo, record, username=None):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            raise GraphQLError("Not permitted")

        likes = LikeModel.objects.filter(type=typeinfo, record_id=record, user_from__kakaoID=kakaoID)
        return likes

    def resolve_comments(self, info, accessToken, record, parent=None):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            raise GraphQLError("Not permitted")

        if parent is not None:
            comments = CommentModel.objects.filter(post_id=record, parent=parent)
        else:
            comments = CommentModel.objects.filter(post_id=record)
        return comments

    def resolve_histories(self, info, accessToken):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            raise GraphQLError("Not permitted")

        histories = HistoryModel.objects.filter(user__kakaoID=kakaoID).order_by("date").reverse()
        return histories

    def resolve_chatrooms(self, info, accessToken, username=None):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            raise GraphQLError("Not permitted")

        if username:
            chatrooms = ChatroomModel.objects.filter(user_from__kakaoID=kakaoID, user_to__name=username)
            chatrooms |= ChatroomModel.objects.filter(user_to__kakaoID=kakaoID, user_from__name=username)
            return chatrooms

        chatrooms = ChatroomModel.objects.filter(user_from__kakaoID=kakaoID)
        chatrooms |= ChatroomModel.objects.filter(user_to__kakaoID=kakaoID)
        return chatrooms

    def resolve_messages(self, info, accessToken, username):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            raise GraphQLError("Not permitted")

        try:
            chatroom = ChatroomModel.objects.get(user_from__kakaoID=kakaoID, user_to__name=username)
        except:
            try:
                chatroom = ChatroomModel.objects.get(user_to__kakaoID=kakaoID, user_from__name=username)
            except:
                raise GraphQLError("Not permitted")

        messages = MessageModel.objects.filter(chatroom_id=chatroom.chatroom_id)
        return messages


class CreateUser(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        name = graphene.String(required=True)
        kakaoID = graphene.Int(required=True)
        accessToken = graphene.String(required=True)

    def mutate(self, info, name, kakaoID, accessToken):
        if verify_kakaoToken(accessToken, kakaoID):
            try:
                user = UserModel.objects.get(kakaoID=kakaoID)
            except:
                user = UserModel(name=name, kakaoID=kakaoID)
                user.save()
            return CreateUser(success=True)
        return CreateUser(success=False)


class UploadProfile(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        img = Upload(required=True)
        accessToken = graphene.String(required=True)

    def mutate(self, info, img, accessToken):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return UploadProfile(success=False)

        User = UserModel.objects.get(kakaoID=kakaoID)
        img.name = str(kakaoID) + "_" + img.name
        User.profile.delete()
        User.profile = img
        User.save()
        return UploadProfile(success=True)


class EditProfile(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        accessToken = graphene.String(required=True)
        img = Upload()
        name = graphene.String()
        text_comment = graphene.String()
        is_open = graphene.Boolean()

    def mutate(self, info, accessToken, img=None, name=None, text_comment=None, is_open=None):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return EditProfile(success=False)

        try:
            User = UserModel.objects.get(kakaoID=kakaoID)
            if img:
                img.name = str(kakaoID) + "_" + img.name
                User.profile.delete()
                User.profile = img
            if name:
                User.name = name
            if text_comment:
                User.text_comment = text_comment
            if is_open:
                User.is_open = is_open

            User.save()
            return EditProfile(success=True)

        except:
            return EditProfile(success=False)


class AddPost(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        accessToken = graphene.String(required=True)
        img = Upload(required=True)
        place = graphene.String()
        hashtag = graphene.String()
        allow_comment = graphene.Boolean()
        comment = graphene.String()

    def mutate(self, info, accessToken, img, place="", allow_comment=True, comment="", hashtag=None):
        if not img:
            return AddPost(success=False)

        kakaoID = get_kakaoID(accessToken)
        if kakaoID is None:
            return AddPost(success=False)

        user = UserModel.objects.get(kakaoID=kakaoID)

        post = PostModel(user=user, allow_comment=allow_comment, place=place, like_count=0, text_comment=comment)
        post.save()
        record_id = post.post_id

        pic = PictureModel(pic=img, type='P', record_id=record_id, pic_idx=0)
        pic.save()

        user.post_count = user.post_count + 1
        user.save()

        if hashtag is not None:
            hash = HashtagModel(tag_name=hashtag, type='P', record_id=record_id)
            hash.save()

        return AddPost(success=True)


class EditPost(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        accessToken = graphene.String(required=True)
        record = graphene.Int(required=True)
        place = graphene.String()
        comment = graphene.String()

    def mutate(self, info, accessToken, record, place=None, comment=None):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return EditPost(success=False)

        user = UserModel.objects.get(kakaoID=kakaoID)
        try:
            post = PostModel.objects.get(user=user, post_id=record)
        except:
            return EditPost(success=False)

        post.text_comment = comment
        post.place = place
        post.save()
        return EditPost(success=True)


class RemovePost(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        accessToken = graphene.String(required=True)
        record = graphene.Int(required=True)

    def mutate(self, info, accessToken, record):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return RemovePost(success=False)

        user = UserModel.objects.get(kakaoID=kakaoID)
        try:
            post = PostModel.objects.get(user=user, post_id=record)
        except:
            return RemovePost(success=False)

        try:
            PictureModel.objects.get(record_id=record, type='P').delete()
        except:
            pass

        try:
            comments = CommentModel.objects.filter(post_id=record)
            try:
                likes = LikeModel.objects.filter(record_id__in=comments, type='C').delete()
            except:
                pass
            comments.delete()
        except:
            pass

        try:
            LikeModel.objects.filter(record_id=record, type='P').delete()
        except:
            pass

        post.delete()

        user.post_count -= 1
        user.save()
        return RemovePost(success=True)


class addFollow(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        accessToken = graphene.String(required=True)
        fkakaoID = graphene.Int(required=True)

    def mutate(self, info, accessToken, fkakaoID):
        kakaoID = get_kakaoID(accessToken)
        if kakaoID is None:
            return addFollow(success=False)

        if kakaoID == fkakaoID:
            return addFollow(success=False)

        try:
            user_from = UserModel.objects.get(kakaoID=kakaoID)
            user_to = UserModel.objects.get(kakaoID=fkakaoID)
        except:
            return addFollow(success=False)

        history = FollowModel.objects.filter(user_from__kakaoID=kakaoID, user_to__kakaoID=fkakaoID)
        if history.exists():
            return addFollow(success=False)

        else:
            follow = FollowModel(user_from=user_from, user_to=user_to)
            follow.save()

            addHistory = HistoryModel(user=user_to, type='F', record_id=follow.follow_id)
            addHistory.save()

            user_to.follower_count += 1
            user_to.save()

            user_from.following_count += 1
            user_from.save()
            return addFollow(success=True)


class unFollow(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        accessToken = graphene.String(required=True)
        fkakaoID = graphene.Int(required=True)
        choice = graphene.Int()

    def mutate(self, info, accessToken, fkakaoID, choice=None):
        kakaoID = get_kakaoID(accessToken)
        if kakaoID is None:
            return addFollow(success=False)

        if kakaoID == fkakaoID:
            return addFollow(success=False)

        try:
            if choice == None:
                user_from = UserModel.objects.get(kakaoID=kakaoID)
                user_to = UserModel.objects.get(kakaoID=fkakaoID)
                history = FollowModel.objects.filter(user_from__kakaoID=kakaoID, user_to__kakaoID=fkakaoID)
                if not history.exists():
                    return addFollow(success=False)
            else:
                user_from = UserModel.objects.get(kakaoID=fkakaoID)
                user_to = UserModel.objects.get(kakaoID=kakaoID)
                history = FollowModel.objects.filter(user_from__kakaoID=fkakaoID, user_to__kakaoID=kakaoID)
                if not history.exists():
                    return addFollow(success=False)
        except:
            return addFollow(success=False)

        else:
            history.delete()

            user_to.follower_count -= 1
            user_to.save()

            user_from.following_count -= 1
            user_from.save()
            return addFollow(success=True)


class addLike(graphene.Mutation):
    success = graphene.Boolean()
    likes = graphene.Int()

    class Arguments:
        accessToken = graphene.String(required=True)
        typeinfo = graphene.String(required=True)
        record = graphene.Int(required=True)

    def mutate(self, info, accessToken, typeinfo, record):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return addLike(success=False)

        elif typeinfo != 'P' and typeinfo != 'C':
            return addLike(success=False)

        if typeinfo == 'P':
            try:
                post = PostModel.objects.get(post_id=record)
                user_to = post.user
                user_from = UserModel.objects.get(kakaoID=kakaoID)

                like = LikeModel(user_from=user_from, user_to=user_to, type=typeinfo, record_id=record)
                like.save()

                if user_to.kakaoID != kakaoID:
                    addHistory = HistoryModel(user=user_to, type='L', record_id=like.like_id)
                    addHistory.save()

                post.like_count += 1
                post.save()

                return addLike(success=True, likes=post.like_count)

            except:
                return addLike(success=False)

        else:
            try:
                comment = CommentModel.objects.get(comment_id=record)
                user_to = comment.user
                user_from = UserModel.objects.get(kakaoID=kakaoID)

                like = LikeModel(user_from=user_from, user_to=user_to, type=typeinfo, record_id=record)
                like.save()

                if user_to.kakaoID != kakaoID:
                    addHistory = HistoryModel(user=user_to, type='L', record_id=like.like_id)
                    addHistory.save()

                comment.like_count += 1
                comment.save()

                return addLike(success=True, likes=comment.like_count)

            except:
                return addLike(success=False)


class unLike(graphene.Mutation):
    success = graphene.Boolean()
    likes = graphene.Int()

    class Arguments:
        accessToken = graphene.String(required=True)
        typeinfo = graphene.String(required=True)
        record = graphene.Int(required=True)

    def mutate(self, info, accessToken, typeinfo, record):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return unLike(success=False)

        elif typeinfo != 'P' and typeinfo != 'C':
            return unLike(success=False)

        if typeinfo == 'P':
            try:
                post = PostModel.objects.get(post_id=record)
                user_to = post.user
                user_from = UserModel.objects.get(kakaoID=kakaoID)

                like = LikeModel.objects.get(user_from=user_from, user_to=user_to, type=typeinfo, record_id=record)
                like.delete()

                post.like_count -= 1
                post.save()

                return unLike(success=True, likes=post.like_count)
            except:
                return unLike(success=False)

        else:
            try:
                user_from = UserModel.objects.get(kakaoID=kakaoID)

                like = LikeModel.objects.get(user_from=user_from, type=typeinfo, record_id=record)
                comment = CommentModel.objects.get(comment_id=like.record_id)

                like.delete()

                comment.like_count -= 1
                comment.save()

                return unLike(success=True, likes=comment.like_count)
            except:
                return unLike(success=False)


class addComment(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        accessToken = graphene.String(required=True)
        record = graphene.Int(required=True)
        text = graphene.String(required=True)
        parent = graphene.Int()

    def mutate(self, info, accessToken, record, text, parent=None):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return addComment(success=False)

        try:
            post = PostModel.objects.get(post_id=record)
            if not post.allow_comment:
                return addComment(success=False)

            user = UserModel.objects.get(kakaoID=kakaoID)
            notice_to = UserModel.objects.get(user_id=post.user_id)

            comment = CommentModel(user=user, post_id=record, text_comment=text)

            if parent:
                try:
                    parent_comment = CommentModel.objects.get(comment_id=parent)
                    if parent_comment.parent:
                        return addComment(success=False)
                    notice_to = UserModel.objects.get(user_id=parent_comment.user_id)

                except:
                    return addComment(success=False)
                comment.parent = parent

            comment.save()

            if user.user_id != notice_to.user_id:
                addHistory = HistoryModel(user=notice_to, type='C', record_id=comment.comment_id)
                addHistory.save()

            post.comment_count += 1
            post.save()
            return addComment(success=True)

        except:
            return addComment(success=False)


class removeComment(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        accessToken = graphene.String(required=True)
        record = graphene.Int(required=True)

    def mutate(self, info, accessToken, record):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return removeComment(success=False)

        try:
            user = UserModel.objects.get(kakaoID=kakaoID)
            comment = CommentModel.objects.get(user=user, comment_id=record)
            comments = CommentModel.objects.filter(parent=comment.comment_id)
            likes = LikeModel.objects.filter(record_id=record, type='C')
            post = PostModel.objects.get(post_id=comment.post_id)
            total_c = comments.count() + 1
            post.comment_count -= total_c
            post.save()
            if likes.count():
                likes.delete()
            if comments.count():
                comments.delete()
            comment.delete()
            return removeComment(success=True)
        except:
            return removeComment(success=False)


class updateHistorySeen(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        accessToken = graphene.String(required=True)
        record = graphene.Int(required=True)

    def mutate(self, info, accessToken, record):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return updateHistorySeen(success=False)

        try:
            user = UserModel.objects.get(kakaoID=kakaoID)
            history = HistoryModel.objects.get(user=user, history_id=record)
            history.seen = True
            history.save()
            return updateHistorySeen(success=True)
        except:
            return updateHistorySeen(success=False)


class deactivateHistory(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        accessToken = graphene.String(required=True)
        record = graphene.Int(required=True)

    def mutate(self, info, accessToken, record):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return deactivateHistory(success=False)

        try:
            user = UserModel.objects.get(kakaoID=kakaoID)
            history = HistoryModel.objects.get(user=user, history_id=record)
            history.is_active = False
            history.save()
            return deactivateHistory(success=True)
        except:
            return deactivateHistory(success=False)


class getHistoryDetail(graphene.Mutation):
    username = graphene.String()
    success = graphene.Boolean()
    profile = graphene.String()

    class Arguments:
        accessToken = graphene.String(required=True)
        record = graphene.Int(required=True)

    def mutate(self, info, accessToken, record):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return deactivateHistory(success=False, username="")

        try:
            history = HistoryModel.objects.get(user__kakaoID=kakaoID, history_id=record)
            if history.type == 'L':
                like = LikeModel.objects.get(like_id=history.record_id)
                return getHistoryDetail(success=True, username=like.user_from.name, profile=like.user_from.profile)
            elif history.type == 'F':
                follow = FollowModel.objects.get(follow_id=history.record_id)
                return getHistoryDetail(success=True, username=follow.user_from.name, profile=follow.user_from.profile)
            elif history.type == 'C':
                comment = CommentModel.objects.get(comment_id=history.record_id)
                return getHistoryDetail(success=True, username=comment.user.name, profile=comment.user.profile)
            else:
                return getHistoryDetail(success=False, username="", profile="")
        except:
            return getHistoryDetail(success=False, username="", profile="")


class createChatroom(graphene.Mutation):
    success = graphene.Boolean()
    id = graphene.Int()

    class Arguments:
        accessToken = graphene.String(required=True)
        username = graphene.String(required=True)

    def mutate(self, info, accessToken, username):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return createChatroom(success=False, id=-1)

        user_from = UserModel.objects.get(kakaoID=kakaoID)
        try:
            user_to = UserModel.objects.get(name=username)
        except:
            return createChatroom(success=False, id=-1)

        if user_from == user_to:
            return createChatroom(success=False, id=-1)

        chatrooms = ChatroomModel.objects.filter(user_from=user_from, user_to=user_to)
        chatrooms |= ChatroomModel.objects.filter(user_to=user_from, user_from=user_to)

        if chatrooms.exists():
            return createChatroom(success=False, id=-1)

        chatroom = ChatroomModel(user_from=user_from, user_to=user_to)
        chatroom.save()
        return createChatroom(success=True, id=chatroom.chatroom_id)


class leaveChatroom(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        accessToken = graphene.String(required=True)
        username = graphene.String(required=True)

    def mutate(self, info, accessToken, username):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return leaveChatroom(success=False)

        user_from = UserModel.objects.get(kakaoID=kakaoID)
        try:
            user_to = UserModel.objects.get(name=username)
        except:
            return leaveChatroom(success=False)

        if user_from == user_to:
            return leaveChatroom(success=False)

        try:
            chatroom = ChatroomModel.objects.get(user_from=user_from, user_to=user_to)
        except:
            try:
                chatroom = ChatroomModel.objects.get(user_from=user_to, user_to=user_from)
            except:
                return leaveChatroom(success=False)

        messages = MessageModel.objects.filter(chatroom_id=chatroom.chatroom_id).delete()
        chatroom.delete()
        return leaveChatroom(success=True)


class sendMessage(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        accessToken = graphene.String(required=True)
        username = graphene.String(required=True)
        chatid = graphene.Int(required=True)
        msg = graphene.String(required=True)

    def mutate(self, info, accessToken, username, chatid, msg):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return sendMessage(success=False)

        me = UserModel.objects.get(kakaoID=kakaoID)
        try:
            you = UserModel.objects.get(name=username)
        except:
            return sendMessage(success=False)

        try:
            chatroom = ChatroomModel.objects.get(chatroom_id=chatid)
            if chatroom.user_from != me and chatroom.user_to != me:
                return sendMessage(success=False)
            if chatroom.user_from != you and chatroom.user_to != you:
                return sendMessage(success=False)
        except:
            return sendMessage(success=False)

        message = MessageModel(sender=me, chatroom_id=chatid, has_seen=False, text_message=msg)
        message.save()
        return sendMessage(success=True)


class deleteMessage(graphene.Mutation):
    success = graphene.Boolean()

    class Arguments:
        accessToken = graphene.String(required=True)
        chatid = graphene.Int(required=True)
        record = graphene.Int(required=True)

    def mutate(self, info, accessToken, chatid, record):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return deleteMessage(success=False)

        me = UserModel.objects.get(kakaoID=kakaoID)

        try:
            chatroom = ChatroomModel.objects.get(chatroom_id=chatid)
            if chatroom.user_from != me and chatroom.user_to != me:
                return deleteMessage(success=False)
        except:
            return deleteMessage(success=False)

        try:
            message = MessageModel.objects.get(sender=me, chatroom_id=chatid, message_id=record)
        except:
            return deleteMessage(success=False)

        message.delete()
        return deleteMessage(True)


class Mutation(graphene.ObjectType):
    create_user = CreateUser.Field()
    upload_profile = UploadProfile.Field()
    edit_profile = EditProfile.Field()
    add_post = AddPost.Field()
    edit_post = EditPost.Field()
    remove_post = RemovePost.Field()
    add_follow = addFollow.Field()
    un_follow = unFollow.Field()
    add_like = addLike.Field()
    un_like = unLike.Field()
    add_comment = addComment.Field()
    remove_comment = removeComment.Field()
    update_history_seen = updateHistorySeen.Field()
    deactivate_history = deactivateHistory.Field()
    get_history_detail = getHistoryDetail.Field()
    create_chatroom = createChatroom.Field()
    leave_chatroom = leaveChatroom.Field()
    send_message = sendMessage.Field()
    deleteMessage = deleteMessage.Field()


schema = graphene.Schema(
    query=Query,
    mutation=Mutation
)
