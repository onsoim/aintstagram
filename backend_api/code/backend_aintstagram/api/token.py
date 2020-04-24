import requests
import json


def verify_kakaoToken(accessToken, kakaoID):
    url = 'https://kapi.kakao.com/v1/user/access_token_info'
    r = requests.get(url, headers={'Authorization': 'Bearer ' + accessToken})
    if r.status_code == 200:
        res = r.json()
        return int(res['id']) == kakaoID
    return False


def get_kakaoID(accessToken):
    url = 'https://kapi.kakao.com/v1/user/access_token_info'
    r = requests.get(url, headers={'Authorization': 'Bearer ' + accessToken})
    if r.status_code == 200:
        res = r.json()
        return res['id']
    return None
