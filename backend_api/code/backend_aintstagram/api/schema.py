import graphene
from graphene_django import DjangoObjectType
from graphene_file_upload.scalars import Upload

from api.models import UserModel


class UserType(DjangoObjectType):
    class Meta:
        model = UserModel


class Query(graphene.ObjectType):
    users = graphene.List(UserType,
                          kakaoID=graphene.Int(),
                          username=graphene.String(),
                          )

    def resolve_users(self, info, kakaoID=None, username=None):
        query = UserModel.objects.all()

        if kakaoID:
            return UserModel.objects.filter(kakaoID=kakaoID)

        elif username:
            return UserModel.objects.filter(name=username)

        else:
            return query


class CreateUser(graphene.Mutation):
    name = graphene.String()
    kakaoID = graphene.Int()
    date = graphene.DateTime()

    class Arguments:
        name = graphene.String(required=True)
        kakaoID = graphene.Int(required=True)

    def mutate(self, info, name, kakaoID):
        try:
            user = UserModel.objects.get(kakaoID=kakaoID)
        except:
            user = UserModel(name=name, kakaoID=kakaoID)
            user.save()
        return CreateUser(
            name=user.name,
            kakaoID=user.kakaoID,
            date=user.date
        )


class UploadProfile(graphene.Mutation):
    class Arguments:
        img = Upload(required=True)
        kakaoID = graphene.Int(required=True)

    success = graphene.Boolean()

    def mutate(self, info, img, kakaoID):
        print(kakaoID)
        return UploadProfile(success=True)

class Mutation(graphene.ObjectType):
    create_user = CreateUser.Field()
    upload_profile = UploadProfile.Field()

schema = graphene.Schema(
    query=Query,
    mutation=Mutation
)