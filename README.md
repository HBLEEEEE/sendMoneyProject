요구사항 구현 여부 및 요구사항 별 특이 사항
1. 회원 가입 API
   1) API 개발 : 완료
   2) 유니크값 userId(이메일 형식) : controller단에서 validation
   3) 암화가 필요한 값 : password와 idValue를 sequrity를 이용해 암호화
   4) idType : 개인회원과 법인회원에 대한 Enum으로 개발
   5) 각 column에 대한 제약사항 자체적으로 개발 후 controller단에서 validation 수행
      - 공통사항 : 공백 및 null 불가
      - password는 4~12자리
      - name은 2~10자리
   
2. 로그인 API
   1) API 개발 : 완료
   2) userId 및 password에 대한 제약사항은 controller단에서 validation

3. 송금 견적서를 갖고 오는 API
   1) API 개발 : 완료
   2) 통화에 따른 수수료와 받는 금액 계산 방법은 Service단 말고 CurrencyServiceFactory에 로직 분류
   3) 일일 한도는 USD로 계산됨으로 어떤 통화로 견적서를 가져오든지 USD도 계산 포함
   4) 환율 정보 조회는 RestTemplate와 URI를 이용함
   5) Java Util Currency에서 제공하는 defaultFractionDigits를 이용해 소수점 자리 반올림
   6) Quote Entity를 만들었고, 해당 견적서가 사용되면 Quote와 Request Entity를 매핑한다.

4. 송금 접수 요청 API
   1) API 개발 : 완료
   2) 일일 한도 예외 처리 수행
   3) 송금 시 Request가 생성되고 Quote에 매핑된다.

5. 회원의 거래 이력을 가지고 오는 API
   1) API 개발 : 완료
   2) 과제 설명과 같이 오래된 순서부터 가져온다.

전체적인 구현 방법
1. Response는 API별 Response 객체를 만들어서 응답
2. 암호화는 Security를 이용해서 진행
3. JWT token 적용
4. controller단의 Validation은 GlobalExceptionHandler를 만들어서 진행
5. 필요한 Error코드와 Success코드를 직접 만들어서 진행
6. DB에서 사용하는 Entity : User, Quote, Request

과제 검증
1. postman을 이용하여 api가 잘 작동하는 지 확인
   - api1 부터 api5까지 순차적으로 실행하면서 기능 테스트 진행
2. 작성한 api의 성공 케이스와 예외 케이스들에 대해서 testcode 작성
   - 성공 케이스 및 예외 케이스들에 대한 unit test 작성
   - 동시성 제어 테스트 수행 : 한번에 같은 요청이 여러번 올때 하나만 수행되는지 확인

과제를 더 진행한다고 했을 때 추가하면 좋다고 생각되는 것들
1. 동시의 다수의 요청이 들어올 때 효과적으로 처리하기 위해서 Message Queue 적용이 필요해보인다.
2. 견적서와 송금 내역 테이블에 대해선 최소 column만 정의했지만, 예외 상황(취소, 환불 등)을 관리하는 column이 필요할 것 같다.
3. 일일 송금 한도 test code의 경우 현재 환율에서 크게 벗어나지 않는 다는 가정하에 작성했는데, 
1달러가 400원이 되거나 하는 극단적인 상황까지 고려하려면 수정이 필요하다.
4. 지금은 환율정보를 요청이 올때 마다 받고있다. 만약 요청이 많아진다면 환율 정보를 불필요하게 많이 요청하게 된다.
Redis와 같은 캐시 서버에 환율 정보를 따로 저장해서, 그 정보를 주기적으로 갱신하고 가져다 쓰는게 좋을 수 도 있을 것 같다.
5. 환율 정보를 한 곳에서만 가져오고 있으기 때문에, 해당 API 문제 발생시 대처 방법이 필요하다.
6. api는 /v1/transfer/quote와 같이 버저닝하고 관리하면 좋으나, 과제 요구 사항에 안맞음으로 진행 안함
