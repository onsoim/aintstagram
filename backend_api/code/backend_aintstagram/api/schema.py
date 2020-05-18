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
            return PostModel.objects.filter(user__kakaoID=kakaoID).order_by('date').reverse()

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

        try:
            post = PostModel.objects.get(post_id=record)
            user_to = post.user
            user_from = UserModel.objects.get(kakaoID=kakaoID)

        except:
            return addLike(success=False)

        history = LikeModel.objects.filter(user_from=user_from, user_to=user_to)
        if history.exists():
            return addLike(success=False)

        else:
            like = LikeModel(user_from=user_from, user_to=user_to, type=typeinfo, record_id=record)
            like.save()

            post.like_count += 1
            post.save()

            return addLike(success=True)


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

schema = graphene.Schema(
    query=Query,
    mutation=Mutation
)
