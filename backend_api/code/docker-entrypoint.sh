django-admin startproject backend_aintstagram
python $(pwd)/backend_aintstagram/manage.py makemigrations
python $(pwd)/backend_aintstagram/manage.py migrate
echo "from django.contrib.auth import get_user_model; User = get_user_model(); User.objects.create_superuser('root', 'wizley@kakao.com', 'alpine')" | python3 $(pwd)/backend_aintstagram/manage.py shell
