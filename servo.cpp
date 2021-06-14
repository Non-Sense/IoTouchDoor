#include <iostream>
#include <cstring>
#include <thread>
#include <chrono>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>

#include <errno.h>

#include <wiringPi.h>

#define SWITCH 17
#define DOOR_SENSOR 23
#define SERVO_SENSOR 24
#define LED1 27
#define LED2 22

#define SERVO_LOCK 43
#define SERVO_UNLOCK 82
#define SERVO_WAIT 800*1000

#define PORT 10000
#define HOST "127.0.0.1"

int sockfd;

void unlock(){
    std::cout << "unlock" << std::endl;
    pwmWrite(18, SERVO_UNLOCK);
    usleep(SERVO_WAIT);
    pwmWrite(18, 0);
}

void lock(){
    std::cout << "lock" << std::endl;
    pwmWrite(18, SERVO_LOCK);
    usleep(SERVO_WAIT);
    pwmWrite(18, 0);
}


bool isLock = true;
bool isClose = true;
bool before_push = false;
void polling(){
    while(true){
        if(digitalRead(DOOR_SENSOR)){
            digitalWrite(LED1,LOW);
            isClose = true;
        }else{
            digitalWrite(LED1,HIGH);
            isClose = false;
        }
        if(digitalRead(SERVO_SENSOR)){
            digitalWrite(LED2,LOW);
            isLock = true;
        }else{
            digitalWrite(LED2,HIGH);
            isLock = false;
        }
	if(!digitalRead(SWITCH)){
	    if(!before_push){
		    std::thread unlock_thread(unlock);
		    unlock_thread.detach();
	    }
            before_push=true;
	}else{
            before_push=false;
	}
        usleep(1000);
    }
}


void on_exit(){
    std::cout << "exit" << std::endl;
    close(sockfd);
}

uint8_t status(){
    uint8_t ret = 0;
    ret|=isLock?1:0;
    ret|=isClose?2:0;
    return ret;
}

int main() {

    using namespace std;
    using std::this_thread::sleep_for;

    if(wiringPiSetupGpio() == -1){
        std::cout << "gpio error"<<std::endl;
        return -1;
    }

    pinMode(SWITCH, INPUT);
    pinMode(DOOR_SENSOR,INPUT);
    pinMode(SERVO_SENSOR,INPUT);
    pinMode(LED1,OUTPUT);
    pinMode(LED2,OUTPUT);

    pinMode(18, PWM_OUTPUT);
    pwmSetMode(PWM_MODE_MS);
    pwmSetClock(400);
    pwmSetRange(1024);

    std::thread th(polling);

    atexit(on_exit);
    while(true) {

        sockfd = socket(AF_INET, SOCK_STREAM, 0);
        if(sockfd < 0){
            cout << "socket error "<<errno << endl;
            continue;
            exit(1);
        }

        struct sockaddr_in addr;
        memset(&addr, 0, sizeof(struct sockaddr_in));

        addr.sin_family = AF_INET;
        addr.sin_port = htons(PORT);
        addr.sin_addr.s_addr = inet_addr(HOST);

        int yes = 1;
        if(setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, (const char*)&yes, sizeof(yes))<0){
            cout << "sockopt error"<<errno<<endl;
            continue;
        }

        if(bind(sockfd, (struct sockaddr*)&addr, sizeof (addr))<0){
            cout << "bind error "<<errno<<endl;
            continue;
            exit(1);
        }

        if(listen(sockfd, SOMAXCONN)<0){
            cout << "listen error" << endl;
            close(sockfd);
            continue;
            exit(1);
        }

        struct sockaddr_in get_addr;
        socklen_t len = sizeof(struct sockaddr_in);

        int connect = accept(sockfd, (struct sockaddr *) &get_addr, &len);

        if (connect < 0) {
            cout << "accept error" << endl;
            close(sockfd);
            sleep_for(chrono::milliseconds(10));
            continue;
            exit(1);
        }


        uint8_t rcv;
        if (recv(connect, &rcv, 1, 0) <= 0) {
            close(sockfd);
            continue;
        }
        switch (rcv) {
            case 1: {
                    std::thread unlock_thread(unlock);
                    unlock_thread.detach();
                }
                break;
            case 2: {
                    std::thread lock_thread(lock);
                    lock_thread.detach();
                }
                break;
            case 3: {
                    uint8_t s = status();
                    send(connect, &s, 1, 0);
                }
                break;
            default:
                break;
        }
        close(sockfd);
        close(connect);
    }
    return 0;
}
