import graphene
from graphene_django import DjangoObjectType
from graphene_file_upload.scalars import Upload
from api.models import *
from api.token import *
from graphql import GraphQLError


class UserType(DjangoObjectType):
    class Meta:
        model = UserModel


class PostType(DjangoObjectType):
    class Meta:
        model = PostModel


class Query(graphene.ObjectType):
    users = graphene.List(UserType,
                          kakaoID=graphene.Int(),
                          username=graphene.String(),
                          accessToken=graphene.String(),
                          )

    posts = graphene.List(PostType,
                          username=graphene.String(),
                          accessToken=graphene.String(required=True),
                          )


    def resolve_users(self, info, kakaoID=None, username=None, accessToken=None):
        query = UserModel.objects.all()

        if kakaoID:
            return UserModel.objects.filter(kakaoID=kakaoID)

        elif username:
            return UserModel.objects.filter(name=username)

        elif accessToken:
            kakaoID = get_kakaoID(accessToken)
            if kakaoID is not None:
                return UserModel.objects.filter(kakaoID=kakaoID)
        else:
            return query


    def resolve_posts(self, info, username, accessToken):
        kakaoID = get_kakaoID(accessToken)

        if kakaoID is None:
            return EditProfile(success=False)

        if username:
            user = UserModel.objects.get(name=username)
            return PostModel.objects.filter(user=user)

        else:
            user = UserModel.objects.get(kakaoID=kakaoID)
            return PostModel.objects.filter(user=user)


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

    def mutate(self, info, accessToken, img=None, place="", allow_comment=True, comment="", hashtag=None):
        if not img:
            return AddPost(success=False)

        kakaoID = get_kakaoID(accessToken)
        if kakaoID is None:
            return AddPost(success=False)

        user = UserModel.objects.get(kakaoID=kakaoID)

        post = PostModel(user=user, allow_comment=allow_comment, place=place, like_count=0, text_comment=comment)
        post.save()
        record_id = post.post_id

        pic = PictureModel(pic=Upload, type='P', record_id=record_id)
        pic.save()

        user.post_count = user.post_count + 1
        user.save()

        if hashtag is not None:
            hash = HashtagModel(tag_name=hashtag, type='P', record_id=record_id)
            hash.save()

        return AddPost(success=True)


class Mutation(graphene.ObjectType):
    create_user = CreateUser.Field()
    upload_profile = UploadProfile.Field()
    edit_profile = EditProfile.Field()
    add_post = AddPost.Field()


schema = graphene.Schema(
    query=Query,
    mutation=Mutation
)
