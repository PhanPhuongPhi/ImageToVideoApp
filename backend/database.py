from sqlalchemy import create_engine, inspect, text
from sqlalchemy.orm import sessionmaker
from models import Base

SQLALCHEMY_DATABASE_URL = "sqlite:///./app.db"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

def init_db():
    Base.metadata.create_all(bind=engine)
    
    # Auto-add missing columns to existing SQLite tables if models have new columns
    inspector = inspect(engine)
    with engine.connect() as conn:
        for table_name, table in Base.metadata.tables.items():
            if inspector.has_table(table_name):
                existing_columns = {col['name'] for col in inspector.get_columns(table_name)}
                for column in table.columns:
                    if column.name not in existing_columns:
                        col_type = column.type.compile(engine.dialect)
                        default_val = ""
                        if column.default is not None and column.default.arg is not None:
                            default_val = f" DEFAULT {column.default.arg}"
                        elif column.nullable:
                            default_val = " DEFAULT NULL"
                        else:
                            default_val = " DEFAULT 0"
                        
                        stmt = text(f"ALTER TABLE {table_name} ADD COLUMN {column.name} {col_type}{default_val}")
                        conn.execute(stmt)
                        conn.commit()

