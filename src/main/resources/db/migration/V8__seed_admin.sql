-- 와장 계정. 가입으로 만들어지지 않고 처음부터 DB 에 존재한다.
-- TODO: 지금은 비밀번호를 평문으로 저장한다. 해싱을 붙이면 새 마이그레이션에서
--       password 를 3주차 문서의 BCrypt Hash 로 UPDATE 한다. (이 파일은 고치지 말 것)
INSERT INTO `user` (email, password, name, github_username, role, status, seminar_id)
VALUES ('admin@wafflestudio.com', 'waggle1234', '와장', 'waffle-admin', 'ADMIN', 'APPROVED', NULL);
