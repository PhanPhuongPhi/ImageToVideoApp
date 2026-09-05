from sqlalchemy import Column, Integer, String, Float, Boolean, ForeignKey, DateTime, Text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship
import datetime

Base = declarative_base()

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True)
    full_name = Column(String, nullable=True)
    hashed_password = Column(String)
    role = Column(String, default="guest") # admin, guest
    credit_balance = Column(Integer, default=0)
    is_locked = Column(Boolean, default=False)
    otp = Column(String, nullable=True)
    otp_expiry = Column(DateTime, nullable=True)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)

    videos = relationship("Video", back_populates="owner")
    transactions = relationship("CreditTransaction", back_populates="user")

class Video(Base):
    __tablename__ = "videos"
    id = Column(String, primary_key=True, index=True) # job_id
    user_id = Column(Integer, ForeignKey("users.id"))
    prompt = Column(Text)
    video_url = Column(String, nullable=True)
    status = Column(String, default="PENDING") # PENDING, PROCESSING, COMPLETED, FAILED
    error_message = Column(String, nullable=True)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)

    owner = relationship("User", back_populates="videos")

class CreditTransaction(Base):
    __tablename__ = "credit_transactions"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    video_id = Column(String, ForeignKey("videos.id"), nullable=True)
    amount = Column(Integer)
    transaction_type = Column(String) # PLUS, MINUS
    reason = Column(String)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)

    user = relationship("User", back_populates="transactions")
    video = relationship("Video")

class CreditPackage(Base):
    __tablename__ = "credit_packages"
    id = Column(String, primary_key=True, index=True)
    name = Column(String)
    price = Column(Float)
    credits = Column(Integer)
    description = Column(String, nullable=True)

class Promotion(Base):
    __tablename__ = "promotions"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String)
    reward_credits = Column(Integer)
    start_date = Column(String)
    end_date = Column(String)
    usage_limit = Column(Integer, default=0) # 0 means unlimited
    current_usage = Column(Integer, default=0)
    is_active = Column(Boolean, default=True)

class PromotionRegistration(Base):
    __tablename__ = "promotion_registrations"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    promotion_id = Column(Integer, ForeignKey("promotions.id"))
    registered_at = Column(DateTime, default=datetime.datetime.utcnow)

class SystemSetting(Base):
    __tablename__ = "settings"
    key = Column(String, primary_key=True, index=True)
    value = Column(String)
    description = Column(String, nullable=True)
