import requests
import json

def test_server():
    url = "https://GhostTester.pythonanywhere.com/api/mixx-sms-payment"
    
    test_data = {
        "sms_content": "Umepokea TSh 300 kutoka kwa 255123456789 - John Doe. Kumbukumbu No.: 25792901157688",
        "sender_number": "255123456789", 
        "timestamp": "2024-01-01 12:00:00"
    }
    
    try:
        response = requests.post(url, json=test_data, timeout=10)
        print(f"Status Code: {response.status_code}")
        print(f"Response: {response.text}")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    test_server()
