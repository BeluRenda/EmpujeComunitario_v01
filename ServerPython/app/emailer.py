"""Simple email helper used by the Python gRPC server.

This module provides two functions used elsewhere in the project:
- send_password_email(to_email, username, generated_password) -> bool
- send_adhesion_notification(to_email, evento_id, org_id_adherente, fecha_hora=None) -> bool

The functions print debug information in dev mode and always catch exceptions
so they don't raise to the caller (prevents notification failures from
breaking user creation flows).
"""

import os
import ssl
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from typing import Optional

from app.config import (
    EMAIL_HOST,
    EMAIL_PORT,
    EMAIL_USER,
    EMAIL_PASSWORD,
    EMAIL_FROM_NAME,
)

EMAIL_DEV_PRINT = os.getenv("EMAIL_DEV_PRINT", "false").lower() in ("1", "true", "yes")


def _send_smtp_message(from_addr: str, to_addr: str, message: MIMEMultipart) -> bool:
    try:
        context = ssl.create_default_context()

        # Try authenticated send if credentials exist
        if EMAIL_USER and EMAIL_PASSWORD:
            try:
                with smtplib.SMTP(EMAIL_HOST, EMAIL_PORT, timeout=30) as server:
                    server.starttls(context=context)
                    server.login(EMAIL_USER, EMAIL_PASSWORD)
                    server.sendmail(from_addr, to_addr, message.as_string())
                    print(f"[EMAIL] Enviado (auth) a {to_addr} via {EMAIL_HOST}:{EMAIL_PORT}")
                    return True
            except Exception as e:
                print(f"[EMAIL] Error enviando con auth: {e}")

        # Try without auth (development SMTP like MailHog)
        try:
            with smtplib.SMTP(EMAIL_HOST, EMAIL_PORT, timeout=30) as server_noauth:
                server_noauth.sendmail(from_addr, to_addr, message.as_string())
                print(f"[EMAIL] Enviado (no-auth) a {to_addr} via {EMAIL_HOST}:{EMAIL_PORT}")
                return True
        except Exception as e:
            print(f"[EMAIL] Error enviando sin auth: {e}")
            return False

    except Exception as e:
        print(f"[EMAIL] Error inesperado en _send_smtp_message: {e}")
        return False


def send_password_email(to_email: str, username: str, generated_password: str) -> bool:
    try:
        subject = "Credenciales de acceso - Empuje Comunitario"
        from_addr = EMAIL_USER or f"no-reply@{os.getenv('HOSTNAME', 'empuje.local')}"

        text = f"Usuario: {username}\nContraseña: {generated_password}\n"
        html = f"<p>Usuario: <strong>{username}</strong></p><p>Contraseña: <strong>{generated_password}</strong></p>"

        if EMAIL_DEV_PRINT or not EMAIL_HOST:
            print(f"[EMAIL DEV] To: {to_email}\nSubject: {subject}\n{text}")
            return True

        msg = MIMEMultipart("alternative")
        msg["Subject"] = subject
        msg["From"] = f"{EMAIL_FROM_NAME} <{from_addr}>"
        msg["To"] = to_email
        msg.attach(MIMEText(text, "plain"))
        msg.attach(MIMEText(html, "html"))

        return _send_smtp_message(from_addr, to_email, msg)

    except Exception as e:
        print(f"[EMAIL ERROR] send_password_email falló: {e}")
        return False


def send_adhesion_notification(
    to_email: Optional[str], evento_id: str, org_id_adherente: int, fecha_hora: Optional[str] = None
) -> bool:
    try:
        used_recipient = to_email or EMAIL_USER or f"no-reply@{os.getenv('HOSTNAME', 'empuje.local')}"
        from_addr = EMAIL_USER or f"no-reply@{os.getenv('HOSTNAME', 'empuje.local')}"

        subject = f"Nueva adhesión al evento {evento_id}"
        text = f"Nueva adhesión: org_adherente={org_id_adherente} al evento={evento_id} fecha={fecha_hora or 'desconocida'}"
        if not to_email:
            text += "\nNota: el organizador no tiene email; esta notificación es copia enviada al sistema."

        html_extra = (
            "<p><em>Nota: el organizador no tiene un email registrado; esta notificación es una copia enviada al sistema.</em></p>"
            if not to_email
            else ""
        )

        html = (
            f"<div><h2>Nueva adhesión a tu evento</h2>"
            f"<p>La organización <strong>{org_id_adherente}</strong> se adhirió al evento <strong>{evento_id}</strong>.</p>"
            f"<p>Fecha: {fecha_hora or 'desconocida'}</p>{html_extra}</div>"
        )

        if EMAIL_DEV_PRINT or not EMAIL_HOST:
            print(f"[EMAIL DEV] To: {used_recipient}\nSubject: {subject}\n{text}")
            return True

        msg = MIMEMultipart("alternative")
        msg["Subject"] = subject
        msg["From"] = f"{EMAIL_FROM_NAME} <{from_addr}>"
        msg["To"] = used_recipient
        msg.attach(MIMEText(text, "plain"))
        msg.attach(MIMEText(html, "html"))

        return _send_smtp_message(from_addr, used_recipient, msg)

    except Exception as e:
        print(f"[EMAIL ERROR] send_adhesion_notification falló: {e}")
        return False
